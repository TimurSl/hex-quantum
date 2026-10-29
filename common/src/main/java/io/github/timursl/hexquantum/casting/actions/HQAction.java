package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.RenderedSpell;
import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.ParenthesizedOperationResult;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs;
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughMedia;
import at.petrak.hexcasting.api.utils.TreeList;
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base class for every HexQuantum pattern.
 * <p>
 * Pops {@link #argc()} iotas, asks {@link #run} for a {@link Result}, checks the media cost, and then pushes the
 * outputs. World changes go in {@link Result#effect} so they run as a normal Hex Casting side effect (after the media
 * is paid, and not at all under Simulate).
 * <p>
 * Both {@code operate} and {@code operateInParens} are implemented here rather than relying on Kotlin interface
 * defaults, so this works no matter how Hex Casting's Kotlin was compiled.
 */
public abstract class HQAction implements Action {
    protected abstract int argc();

    protected abstract Result run(List<Iota> args, CastingEnvironment env, CastingImage image);

    @Override
    public OperationResult operate(CastingEnvironment env, CastingImage image, SpellContinuation continuation) {
        TreeList<Iota> stack = image.getStack();
        int argc = argc();
        if (argc > stack.size()) {
            throw new MishapNotEnoughArgs(argc, stack.size());
        }
        List<Iota> args = new ArrayList<>(stack.takeRight(argc));
        TreeList<Iota> rest = stack.dropRight(argc);

        Result result = run(args, env, image);

        List<OperatorSideEffect> sideEffects = new ArrayList<>();
        if (result.cost > 0) {
            if (env.extractMedia(result.cost, true) > 0) {
                throw new MishapNotEnoughMedia(result.cost);
            }
            sideEffects.add(new OperatorSideEffect.ConsumeMedia(result.cost));
        }
        if (result.effect != null) {
            sideEffects.add(new OperatorSideEffect.AttemptSpell(new Effect(result.effect), true, true));
        }
        for (ParticleSpray spray : result.particles) {
            sideEffects.add(new OperatorSideEffect.Particles(spray));
        }

        TreeList<Iota> newStack = result.replaceStack != null ? result.replaceStack : rest.appendedAll(result.outputs);
        CastingImage newImage = image.copy(
            newStack,
            image.getParenCount(),
            image.getParenthesized(),
            image.getEscapeNext(),
            image.getSimulateNext(),
            image.getOpsConsumed() + 1,
            image.getComponents()
        );
        SpellContinuation next = result.continuation != null ? result.continuation : continuation;
        var sound = result.effect != null ? HexEvalSounds.SPELL.get() : HexEvalSounds.NORMAL_EXECUTE.get();
        return new OperationResult(newImage, sideEffects, next, sound);
    }

    @Override
    public ParenthesizedOperationResult operateInParens(CastingEnvironment env, CastingImage image,
        SpellContinuation continuation, Iota thisIota) {
        return new ParenthesizedOperationResult(
            image.withNewParenthesized(thisIota, false),
            List.of(),
            continuation,
            HexEvalSounds.NORMAL_EXECUTE.get(),
            ResolvedPatternType.ESCAPED
        );
    }

    /**
     * What a pattern does. Build with the static factories and the fluent setters.
     */
    public static final class Result {
        long cost;
        final List<Iota> outputs = new ArrayList<>();
        @Nullable Consumer<CastingEnvironment> effect;
        final List<ParticleSpray> particles = new ArrayList<>();
        @Nullable SpellContinuation continuation;
        @Nullable TreeList<Iota> replaceStack;

        public static Result of(long cost) {
            Result r = new Result();
            r.cost = cost;
            return r;
        }

        public Result push(Iota... iotas) {
            outputs.addAll(List.of(iotas));
            return this;
        }

        public Result effect(Consumer<CastingEnvironment> effect) {
            this.effect = effect;
            return this;
        }

        public Result particles(ParticleSpray spray) {
            this.particles.add(spray);
            return this;
        }

        /**
         * Replace the continuation (e.g. {@link SpellContinuation.Done} to halt).
         */
        public Result continuation(SpellContinuation continuation) {
            this.continuation = continuation;
            return this;
        }

        /**
         * Replace the whole stack instead of pushing outputs.
         */
        public Result replaceStack(TreeList<Iota> stack) {
            this.replaceStack = stack;
            return this;
        }
    }

    private record Effect(Consumer<CastingEnvironment> body) implements RenderedSpell {
        @Override
        public void cast(CastingEnvironment env) {
            body.accept(env);
        }

        @Override
        public CastingImage cast(CastingEnvironment env, CastingImage image) {
            cast(env);
            return null;
        }
    }
}

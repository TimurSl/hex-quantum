package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs;
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughMedia;
import at.petrak.hexcasting.api.utils.TreeList;
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds;
import io.github.timursl.hexquantum.casting.HQArgs;
import io.github.timursl.hexquantum.casting.WaitScheduler;
import io.github.timursl.hexquantum.casting.mishaps.MishapQuantum;
import io.github.timursl.hexquantum.config.HQConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Rune of Waiting: pop N seconds, pause the hex, and resume it N seconds later with the same stack.
 * <p>
 * The running VM is halted here (its continuation becomes Done and its stack is handed over), and the rest of the
 * hex is resumed by {@link WaitScheduler}.
 */
public class OpWait extends HQAction {
    @Override
    protected int argc() {
        return 1;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        throw new IllegalStateException("OpWait overrides operate");
    }

    @Override
    public OperationResult operate(CastingEnvironment env, CastingImage image, SpellContinuation continuation) {
        TreeList<Iota> stack = image.getStack();
        if (stack.size() < 1) {
            throw new MishapNotEnoughArgs(1, stack.size());
        }
        List<Iota> args = new ArrayList<>(stack.takeRight(1));
        double seconds = HQArgs.seconds(args, 0, 1, HQConfig.WAIT_MAX_SECONDS.value());
        var caster = env.getCastingEntity();
        if (caster != null && WaitScheduler.pendingFor(caster.getUUID()) >= HQConfig.WAIT_MAX_PENDING.value()) {
            throw new MishapQuantum("too_many_waits", HQConfig.WAIT_MAX_PENDING.value());
        }
        long cost = HQConfig.WAIT_COST.media();
        if (cost > 0 && env.extractMedia(cost, true) > 0) {
            throw new MishapNotEnoughMedia(cost);
        }

        CastingImage resumed = image.copy(stack.dropRight(1), image.getParenCount(), image.getParenthesized(),
            image.getEscapeNext(), image.getSimulateNext(), 0L, image.getUserData());
        int ticks = Math.max(1, (int) Math.round(seconds * 20));

        List<OperatorSideEffect> sideEffects = new ArrayList<>();
        if (cost > 0) {
            sideEffects.add(new OperatorSideEffect.ConsumeMedia(cost));
        }
        sideEffects.add(new OperatorSideEffect.AttemptSpell(new ScheduleWait(resumed, continuation, ticks), false, false));

        // The current VM ends here with an empty stack; the resumed VM continues with the real one.
        CastingImage halted = image.copy(TreeList.empty(), image.getParenCount(), image.getParenthesized(),
            image.getEscapeNext(), image.getSimulateNext(), image.getOpsConsumed() + 1, image.getUserData());
        return new OperationResult(halted, sideEffects, SpellContinuation.Done.INSTANCE, HexEvalSounds.NORMAL_EXECUTE.get());
    }

    private record ScheduleWait(CastingImage image, SpellContinuation continuation, int ticks)
        implements at.petrak.hexcasting.api.casting.RenderedSpell {
        @Override
        public void cast(CastingEnvironment env) {
            WaitScheduler.schedule(env, image, continuation, ticks);
        }

        @Override
        public CastingImage cast(CastingEnvironment env, CastingImage image) {
            cast(env);
            return null;
        }
    }
}

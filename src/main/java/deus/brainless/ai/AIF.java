package deus.brainless.ai;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class AIF<CTX> implements Supplier<AI<CTX>> {

	private final Consumer<AI.Brain> brainConfig;
	private final Consumer<AI<CTX>> queueConfig;

	private AIF(Consumer<AI.Brain> brainConfig, Consumer<AI<CTX>> queueConfig) {
		this.brainConfig = brainConfig;
		this.queueConfig = queueConfig;
	}

	public static <CTX> AIF<CTX> base(Consumer<AI.Brain> brainSetup, Consumer<AI<CTX>> queueSetup) {
		return new AIF<>(brainSetup, queueSetup);
	}


	@SuppressWarnings("unchecked")
	public <NCTX extends CTX> AIF<NCTX> extend(Consumer<AI.Brain> extraBrainSetup, Consumer<AI<NCTX>> extraQueueSetup) {
		return new AIF<>(
			this.brainConfig.andThen(extraBrainSetup),
			newQueue -> {
				this.queueConfig.accept((AI<CTX>) newQueue);
				extraQueueSetup.accept(newQueue);
			}
		);
	}

	public AIF<CTX> extendBrain(Consumer<AI.Brain> extraBrainSetup) {
		return new AIF<>(this.brainConfig.andThen(extraBrainSetup), this.queueConfig);
	}

	public AIF<CTX> extendQueue(Consumer<AI<CTX>> extraQueueSetup) {
		return new AIF<>(this.brainConfig, this.queueConfig.andThen(extraQueueSetup));
	}

	@Override
	public AI<CTX> get() {
		return AI.factory(brainConfig, queueConfig).get();
	}
}

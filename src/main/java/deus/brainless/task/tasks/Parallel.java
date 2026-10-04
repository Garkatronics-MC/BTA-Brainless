package deus.brainless.task.tasks;

import deus.brainless.task.StopReason;
import deus.brainless.task.Task;
import deus.brainless.task.TaskRun;
import deus.brainless.task.TaskStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Ejecuta varias tasks a la vez.
 * ALL: exito cuando todas tienen exito; falla en cuanto una falla.
 * ANY: exito en cuanto una tiene exito; falla cuando todas han fallado.
 * Al decidirse, cancela las que sigan corriendo.
 */
public class Parallel implements Task {

	public enum Policy { ALL, ANY }

	private final List<Supplier<Task>> children;
	private final Policy policy;
	private final List<TaskRun<Void>> runs = new ArrayList<>();

	public Parallel(Policy policy, List<Supplier<Task>> children) {
		this.policy = policy;
		this.children = List.copyOf(children);
	}

	@SafeVarargs
	public static Parallel all(Supplier<Task>... children) {
		return new Parallel(Policy.ALL, List.of(children));
	}

	@SafeVarargs
	public static Parallel any(Supplier<Task>... children) {
		return new Parallel(Policy.ANY, List.of(children));
	}

	@Override
	public void start() {
		runs.clear();
		for (Supplier<Task> c : children) {
			TaskRun<Void> run = new TaskRun<>(c.get(), null);
			runs.add(run);
			run.start();
		}
	}

	@Override
	public TaskStatus tick() {
		if (runs.isEmpty()) return TaskStatus.SUCCESS;

		int succeeded = 0, failed = 0;
		for (TaskRun<Void> run : runs) {
			TaskStatus s = run.tick();   // las ya terminadas devuelven su resultado sin ejecutarse
			if (s == TaskStatus.SUCCESS) succeeded++;
			else if (s == TaskStatus.FAILURE) failed++;
		}

		if (policy == Policy.ALL) {
			if (failed > 0) return TaskStatus.FAILURE;
			if (succeeded == runs.size()) return TaskStatus.SUCCESS;
		} else {
			if (succeeded > 0) return TaskStatus.SUCCESS;
			if (failed == runs.size()) return TaskStatus.FAILURE;
		}
		return TaskStatus.RUNNING;
	}

	@Override
	public void stop(StopReason reason) {
		for (TaskRun<Void> run : runs) run.cancel();   // solo afecta a las que siguen corriendo
	}

	@Override
	public boolean interruptible() {
		return runs.stream().allMatch(TaskRun::canInterrupt);
	}
}

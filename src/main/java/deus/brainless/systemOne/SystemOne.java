package deus.brainless.systemOne;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.*;

public class SystemOne {

	public static final String QT_NOUL = "noul";
	public static final String QT_CHOICE = "choice";
	public static final String QT_SCORE = "score";

	public static class Request {

		public final Object state;
		public final String model;
		public final Map<String, Question> questions = new HashMap<>();

		public Request(Object state, String model) {
			this.state = state;
			this.model = model;
		}

		public Request ask(String key, Question question) {
			this.questions.put(key, question);
			return this;
		}


		public Map<String, Object> toMap() {
			Map<String, Object> result = new LinkedHashMap<>();
			result.put("state", this.state);
			result.put("model", this.model);

			Map<String, Map<String, Object>> qs = new HashMap<>();
			this.questions.forEach((s, q)->{
				qs.put(s, q.toMap());
			});

			result.put("questions", qs);

			return result;
		}
	}

	public static Request request(Object state, String model) {
		return new Request(state, model);
	}

	public abstract static class Question {
		private final String type;
		private final Object instructions;

		protected Question(String type, Object instructions) {
			this.type = Objects.requireNonNull(type, "type");
			this.instructions = Objects.requireNonNull(instructions, "instructions");
		}

		protected abstract Object criteria();

		public Map<String, Object> toMap() {
			Map<String, Object> result = new LinkedHashMap<>();
			result.put("type", type);
			result.put("instructions", instructions);
			Object criteria = criteria();
			if (criteria != null) {
				result.put("criteria", criteria);
			}
			return result;
		}
	}

	public static class Noul extends Question {
		private final Map<String, Object> criteria; // null si no se da

		public Noul(Object instructions) { this(instructions, null, null); }

		public Noul(Object instructions, Object yes, Object no) {
			super(QT_NOUL, instructions);
			if (yes == null && no == null) { criteria = null; return; }
			criteria = new LinkedHashMap<>();
			if (yes != null) criteria.put("true", yes);
			if (no != null) criteria.put("false", no);
		}

		@Override protected Object criteria() { return criteria; }
	}

	public static class Choice extends Question {
		private final Map<String, Object> options;

		public Choice(Object instructions, Map<String, Object> options) {
			super(QT_CHOICE, instructions);
			if (options.isEmpty() || options.size() > 255)
				throw new IllegalArgumentException("Choice needs between 1 y 255 options");
			this.options = new LinkedHashMap<>(options);
		}

		@Override protected Object criteria() { return options; }
	}

	public static class Score extends Question {
		private final List<Object> levels;

		public Score(Object instructions, List<?> levels) {
			super(QT_SCORE, instructions);
			if (levels.size() < 2 || levels.size() > 10)
				throw new IllegalArgumentException("Score must have between 2 and 10 levels");
			this.levels = new ArrayList<>(levels);
		}

		@Override protected Object criteria() { return levels; }
	}

	public static Noul noul(Object instructions) {
		return new Noul(instructions);
	}

	public static Noul noul(Object instructions, Object yes, Object no) {
		return new Noul(instructions, yes, no);
	}

	public static Score score(Object instructions, List<?> levels) {
		return new Score(instructions, levels);
	}

	public static Choice choice(Object instructions, Map<String, Object> options) {
		return new Choice(instructions, options);
	}

	// ! Answers

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
	@JsonSubTypes({
		@JsonSubTypes.Type(value = NoulAnswer.class, name = QT_NOUL),
		@JsonSubTypes.Type(value = ChoiceAnswer.class, name = QT_CHOICE),
		@JsonSubTypes.Type(value = ScoreAnswer.class, name = QT_SCORE)
	})
	@JsonIgnoreProperties(ignoreUnknown = true)
	public interface Answer {}

	public record NoulAnswer(double noul) implements Answer {}

	public record ChoiceAnswer(String choice, Map<String, Double> probabilities, double confidence) implements Answer {}

	public record ScoreAnswer(double score, Map<String, String> legend, Map<String, Double> probabilities, double confidence) implements Answer {}

	public record Usage(
		@JsonProperty("input_tokens") int inputTokens,
		@JsonProperty("output_tokens") int outputTokens
	) {}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Response(String model, Map<String, Answer> answers, Usage usage) {
		public <T extends Answer> T get(String key, Class<T> type) {
			Answer a = answers.get(key);
			if (!type.isInstance(a))
				throw new IllegalStateException("'" + key + "' is " + (a == null ? "missing" : a.getClass().getSimpleName()));
			return type.cast(a);
		}

		public NoulAnswer getNoul(String key) {
			return get(key, NoulAnswer.class);
		}
		public ChoiceAnswer getChoice(String key) {
			return get(key, ChoiceAnswer.class);
		}
		public ScoreAnswer getScore(String key) {
			return get(key, ScoreAnswer.class);
		}
	}

}

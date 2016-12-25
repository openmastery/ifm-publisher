package org.openmastery.publisher.api.ideaflow

import org.joda.time.LocalDateTime
import org.openmastery.publisher.api.event.EventType
import org.openmastery.time.MockTimeService
import spock.lang.Specification

public class IdeaFlowTimelineSpec extends Specification {

	private LocalDateTime startTime
	private MockTimeService mockTimeService = new MockTimeService()
	private IdeaFlowTimelineBuilder builder = new IdeaFlowTimelineBuilder(mockTimeService)

	def setup() {
		startTime = mockTimeService.now()
	}

	def "should return timeline if no subtask events defined"() {
		given:
		IdeaFlowTimeline timeline = builder.activate()
				.troubleshootingHours(1).advanceHours(1)
				.deactivate()
				.build()

		when:
		List<IdeaFlowTimeline> timelines = timeline.splitBySubtask()

		then:
		assert timeline.is(timelines[0])
		assert timelines.size() == 1
	}

	def "should return timeline if subtask declared at timeline start"() {
		IdeaFlowTimeline timeline = builder.activate()
				.subtask()
				.troubleshootingHours(1).advanceHours(1)
				.deactivate()
				.build()

		when:
		List<IdeaFlowTimeline> timelines = timeline.splitBySubtask()

		then:
		assert timeline.is(timelines[0])
		assert timelines.size() == 1
	}

	def "should return timeline if subtask declared at timeline end"() {
		IdeaFlowTimeline timeline = builder.activate()
				.troubleshootingHours(1).advanceHours(1)
				.subtask()
				.deactivate()
				.build()

		when:
		List<IdeaFlowTimeline> timelines = timeline.splitBySubtask()

		then:
		assert timeline.is(timelines[0])
		assert timelines.size() == 1
	}

	private List<IdeaFlowTimelineValidator> splitBySubtaskAndCreateValidators(IdeaFlowTimeline timeline) {
		timeline.splitBySubtask().collect {
			new IdeaFlowTimelineValidator(it)
		}
	}

	private void assertValidationComplete(List<IdeaFlowTimelineValidator> validatorList) {
		for (int i = 0; i < validatorList.size(); i++) {
			println "Asserting validation complete on validator ${i}"
			validatorList[i].assertValidationComplete()
		}
	}

	def "should return two sub-timelines and split elements if subtask started in middle of timeline"() {
		given:
		IdeaFlowTimeline timeline = builder.activate()
				.subtask()
				.strategyHours(2)
				.readCodeAndAdvanceMinutes(30)
				.execute()
				.readCodeAndAdvanceMinutes(30)
				.subtask()
				.readCodeAndAdvanceHours(1)
				.deactivate()
				.build()

		when:
		List<IdeaFlowTimelineValidator> validators = splitBySubtaskAndCreateValidators(timeline)

		then:
		validators[0].assertStrategyBand(0, startTime, startTime.plusHours(1))
		validators[0].assertEvents(1, EventType.ACTIVATE)
		validators[0].assertEvents(1, EventType.SUBTASK)
		validators[0].assertExecutionEvents(1)
		validators[0].assertModificationActivity(59)

		and:
		validators[1].assertStrategyBand(0, startTime.plusHours(1), startTime.plusHours(2))
		validators[1].assertModificationActivity(61)
		validators[1].assertEvents(1, EventType.DEACTIVATE)
		validators[1].assertEvents(1, EventType.SUBTASK)

		and:
		assertValidationComplete(validators)
		assert validators.size() == 2
	}

	def "should spit band multiple times if necessary"() {
		given:
		IdeaFlowTimeline timeline = builder.activate()
				.subtask()
				.strategyHours(6)
				.awesome().advanceHours(2)
				.subtask()
				.awesome().advanceHours(2)
				.subtask().advanceHours(2)
				.deactivate()
				.build()

		when:
		List<IdeaFlowTimelineValidator> validators = splitBySubtaskAndCreateValidators(timeline)

		then:
		validators[0].assertStrategyBand(0, startTime, startTime.plusHours(2))
		validators[0].assertEvents(1, EventType.ACTIVATE)
		validators[0].assertEvents(1, EventType.SUBTASK)
		validators[0].assertEvents(1, EventType.AWESOME)

		and:
		validators[1].assertStrategyBand(0, startTime.plusHours(2), startTime.plusHours(4))
		validators[1].assertEvents(1, EventType.SUBTASK)
		validators[1].assertEvents(2, EventType.AWESOME)

		and:
		validators[2].assertStrategyBand(0, startTime.plusHours(4), startTime.plusHours(6))
		validators[2].assertEvents(1, EventType.SUBTASK)
		validators[2].assertEvents(1, EventType.DEACTIVATE)

		and:
		assertValidationComplete(validators)
		assert validators.size() == 3
	}

//	def "should split nested bands"() {
//		given:
//		IdeaFlowTimeline timeline = builder.activate()
//				.wtf()
//				.strategyHours(2).advanceHours(1)
//				.nestedTroubleshootingHours(1)
//
//				.execute()
//				.readCodeAndAdvanceMinutes(30)
//				.subtask()
//				.readCodeAndAdvanceHours(1)
//				.deactivate()
//				.build()
//
//	}

}
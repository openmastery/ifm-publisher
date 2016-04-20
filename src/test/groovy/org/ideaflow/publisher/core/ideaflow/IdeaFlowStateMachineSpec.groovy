package org.ideaflow.publisher.core.ideaflow

import org.ideaflow.publisher.api.IdeaFlowState
import org.ideaflow.publisher.api.IdeaFlowStateType
import spock.lang.Specification

import static org.ideaflow.publisher.api.IdeaFlowStateType.*;

class IdeaFlowStateMachineSpec extends Specification {

	IdeaFlowStateMachine stateMachine = new IdeaFlowStateMachine()
	IdeaFlowInMemoryPersistenceService persistenceService = new IdeaFlowInMemoryPersistenceService()

	def setup() {
		stateMachine.ideaFlowPersistenceService = persistenceService;
	}

	private IdeaFlowState getPersistedState(IdeaFlowStateType type) {
		persistenceService.stateList.find { it.type == type }
	}

	private List<IdeaFlowState> getPersistedStatesOrderdByStartTime() {
		persistenceService.stateList.sort { it.start }
	}

	private void assertActiveState(IdeaFlowStateType expectedType) {
		assert persistenceService.activeState.isOfType(expectedType)
	}

	private void assertContainingState(IdeaFlowStateType expectedType) {
		if (expectedType == null) {
			assert persistenceService.containingState == null
		} else {
			assert persistenceService.containingState.isOfType(expectedType)
		}
	}

	private void assertExpectedStates(IdeaFlowStateType ... expectedTypes) {
		List<IdeaFlowState> states = getPersistedStatesOrderdByStartTime()
		for (int i = 0; i < expectedTypes.length; i++) {
			assert states.size() > i : "Expected types=${expectedTypes}, actual states=${states}"
			assert states[i].type == expectedTypes[i]
			assert states[i].end != null
		}
		assert states.size() == expectedTypes.length
	}

	/* Starting new states without ending old states */

	def "WHEN Progress then start Conflict SHOULD end Progress and start Conflict"() {
		when:
		stateMachine.startTask()
		stateMachine.startConflict("question")
		stateMachine.stopConflict("resolution")

		then:
		assertExpectedStates(PROGRESS, CONFLICT)
	}

	def "WHEN Learning then start Rework SHOULD link Rework state to previous Learning state"() {
		when:
		stateMachine.startTask()
		stateMachine.startLearning("learning")
		stateMachine.startRework("rework")

		then:
		assertExpectedStates(PROGRESS, LEARNING)
		assertActiveState(REWORK)
		assert persistenceService.activeState.isLinkedToPrevious()
		assert getPersistedState(LEARNING).endingComment == "rework" //TODO is this what we really want to do?
	}

	def "WHEN Rework then start Learning SHOULD link Learning state to previous Rework state"() {
		when:
		stateMachine.startTask()
		stateMachine.startRework("rework")
		stateMachine.startLearning("learning")
		stateMachine.stopLearning("learning")

		then:
		assertExpectedStates(PROGRESS, REWORK, LEARNING)
		assertActiveState(PROGRESS)
		assert getPersistedState(LEARNING).isLinkedToPrevious()
	}

	def "WHEN Conflict then start Learning SHOULD link Learning state to previous Conflict state"() {
		when:
		stateMachine.startTask()
		stateMachine.startConflict("conflict")
		stateMachine.startLearning("learning")
		stateMachine.stopLearning("learning")

		then:
		assertExpectedStates(PROGRESS, CONFLICT, LEARNING)
		assert getPersistedState(LEARNING).isLinkedToPrevious()
		assert getPersistedState(CONFLICT).endingComment == "learning" //TODO is this what we want? conflict resolution is learning comment
	}

	def "WHEN Conflict then start Rework SHOULD link Rework state to previous Conflict state"() {
		when:
		stateMachine.startTask()
		stateMachine.startConflict("conflict")
		stateMachine.startRework("rework")
		stateMachine.stopRework("rework")

		then:
		assertExpectedStates(PROGRESS, CONFLICT, REWORK)
		assert getPersistedState(REWORK).isLinkedToPrevious()
	}

	def "WHEN Learning then start Conflict SHOULD transition to a LearningNestedConflict state"() {
		when:
		stateMachine.startTask()
		stateMachine.startLearning("learning start")
		stateMachine.startConflict("conflict start")

		then:
		assertExpectedStates(PROGRESS)
		assertActiveState(CONFLICT)
		assertContainingState(LEARNING)

		when:
		stateMachine.stopConflict("conflict stop")

		then:
		assertExpectedStates(PROGRESS, CONFLICT)
		assert getPersistedState(CONFLICT).isNested()
		assertActiveState(LEARNING)
		assertContainingState(null)
	}

	def "WHEN Rework then start Conflict SHOULD transition to a ReworkNestedConflict state"() {
		when:
		stateMachine.startTask()
		stateMachine.startRework("rework")
		stateMachine.startConflict("conflict")
		stateMachine.stopConflict("conflict")

		then:
		assertExpectedStates(PROGRESS, CONFLICT)
		assertActiveState(REWORK)
		assert getPersistedState(CONFLICT).isNested()

		when:
		stateMachine.startConflict("conflict")
		stateMachine.stopConflict("conflict")
		stateMachine.stopRework("rework")

		then:
		//TODO failing intermittently in sequence PROGRESS, CONFLICT, REWORK, CONFLICT
//[IdeaFlowState(type=PROGRESS, taskId=null, start=2016-04-19T18:39:48.819, end=2016-04-19T18:39:48.819, startingComment=null, endingComment=null, isLinkedToPrevious=false, isNested=false), IdeaFlowState(type=CONFLICT, taskId=null, start=2016-04-19T18:39:48.819, end=2016-04-19T18:39:48.819, startingComment=conflict, endingComment=conflict, isLinkedToPrevious=false, isNested=true), IdeaFlowState(type=REWORK, taskId=null, start=2016-04-19T18:39:48.819, end=2016-04-19T18:39:48.820, startingComment=rework, endingComment=rework, isLinkedToPrevious=false, isNested=false), IdeaFlowState(type=CONFLICT, taskId=null, start=2016-04-19T18:39:48.820, end=2016-04-19T18:39:48.820, startingComment=conflict, endingComment=conflict, isLinkedToPrevious=false, isNested=true)]

		assertExpectedStates(PROGRESS, REWORK, CONFLICT, CONFLICT)
		assertActiveState(PROGRESS)
		assertContainingState(null)
	}

	/* Explicitly ending states */

	def "WHEN Learning then stop Learning SHOULD transition to Progress"() {
		when:
		stateMachine.startTask()
		stateMachine.startLearning("learning start")
		stateMachine.stopLearning("learning stop")

		then:
		assertExpectedStates(PROGRESS, LEARNING)
		assertActiveState(PROGRESS)
		assertContainingState(null)
	}

	def "WHEN Rework then stop Rework SHOULD transition to Progress"() {
		when:
		stateMachine.startTask()
		stateMachine.startRework("rework start")
		stateMachine.stopRework("rework stop")

		then:
		assertExpectedStates(PROGRESS, REWORK)
		assertActiveState(PROGRESS)
		assertContainingState(null)
	}

	def "WHEN Conflict then stop Conflict SHOULD transition to Progress"() {
		when:
		stateMachine.startTask()
		stateMachine.startConflict("conflict")
		stateMachine.stopConflict("conflict")

		then:
		assertExpectedStates(PROGRESS, CONFLICT)
		assertActiveState(PROGRESS)
	}

	def "WHEN LearningNestedConflict then stop Conflict SHOULD transition to prior Learning state"() {
		when:
		stateMachine.startTask()
		stateMachine.startLearning("learning")
		stateMachine.startConflict("conflict")
		stateMachine.stopConflict("conflict")

		then:
		assertExpectedStates(PROGRESS, CONFLICT)
		assertActiveState(LEARNING)
	}

	def "WHEN ReworkNestedConflict then stop Conflict SHOULD transition to prior Rework state"() {
		when:
		stateMachine.startTask()
		stateMachine.startRework("rework")
		stateMachine.startConflict("conflict")
		stateMachine.stopConflict("conflict")

		then:
		assertExpectedStates(PROGRESS, CONFLICT)
		assertActiveState(REWORK)
	}

	def "WHEN LearningNestedConflict then stop Learning SHOULD unnest the Conflict (same conflict)"() {
		when:
		stateMachine.startTask()
		stateMachine.startLearning("learning")
		stateMachine.startConflict("conflict")

		then:
		assertContainingState(LEARNING)
		assertActiveState(CONFLICT)
		assert persistenceService.activeState.isNested()

		when:
		stateMachine.stopLearning("learning")

		then:
		assertExpectedStates(PROGRESS, LEARNING)
		assertActiveState(CONFLICT)
		assertContainingState(null);
		assert persistenceService.activeState.isNested() == false
	}

	def "WHEN ReworkNestedConflict then stop Rework SHOULD unnest the Conflict (same conflict)"() {
		when:
		stateMachine.startTask()
		stateMachine.startRework("rework")
		stateMachine.startConflict("conflict")

		then:
		assertContainingState(REWORK)
		assertActiveState(CONFLICT)
		assert persistenceService.activeState.isNested()

		when:
		stateMachine.stopRework("rework")

		then:
		assertExpectedStates(PROGRESS, REWORK)
		assertActiveState(CONFLICT)
		assertContainingState(null);
		assert persistenceService.activeState.isNested() == false
	}

	def "WHEN LearningNestedConflict SHOULD NOT allow start Rework (disabled)"() {
		given:
		stateMachine.startTask()
		stateMachine.startLearning("learning start")
		stateMachine.startConflict("conflict start")

		when:
		stateMachine.startRework("rework start")

		then:
		thrown(IdeaFlowStateMachine.InvalidTransitionException)
	}

	def "WHEN ReworkNestedConflict SHOULD NOT allow start Learning (disabled)"() {
		given:
		stateMachine.startTask()
		stateMachine.startRework("rework start")
		stateMachine.startConflict("conflict start")

		when:
		stateMachine.startLearning("learning start")

		then:
		thrown(IdeaFlowStateMachine.InvalidTransitionException)
	}

}

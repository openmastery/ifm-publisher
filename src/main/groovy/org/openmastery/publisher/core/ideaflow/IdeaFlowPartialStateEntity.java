package org.openmastery.publisher.core.ideaflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openmastery.publisher.api.ideaflow.IdeaFlowStateType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;
import java.time.LocalDateTime;

@IdClass(IdeaFlowPartialStateEntity.PrimaryKey.class)
@Entity(name = "idea_flow_partial_state")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdeaFlowPartialStateEntity {

	@Id
	private long taskId;

	@Id
	@Enumerated(EnumType.STRING)
	private IdeaFlowPartialStateScope scope;

	@Enumerated(EnumType.STRING)
	private IdeaFlowStateType type;

	@Column(name = "start_time")
	private LocalDateTime start;

	private String startingComment;

	private boolean isLinkedToPrevious;
	private boolean isNested;

	public boolean isOfType(IdeaFlowStateType... typesToCheck) {
		for (IdeaFlowStateType typeToCheck : typesToCheck) {
			if (typeToCheck == type) {
				return true;
			}
		}
		return false;
	}


	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PrimaryKey implements Serializable {
		private long taskId;
		private IdeaFlowPartialStateScope scope;
	}

}
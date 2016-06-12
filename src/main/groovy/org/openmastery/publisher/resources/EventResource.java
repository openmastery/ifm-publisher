package org.openmastery.publisher.resources;

import org.openmastery.publisher.api.event.NewEvent;
import org.openmastery.publisher.api.event.EventType;
import org.openmastery.publisher.api.ResourcePaths;
import org.openmastery.publisher.core.event.EventEntity;
import org.openmastery.time.TimeService;
import org.openmastery.publisher.core.IdeaFlowPersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Component
@Path(ResourcePaths.EVENT_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class EventResource {

	@Autowired
	private TimeService timeService;
	@Autowired
	private IdeaFlowPersistenceService persistenceService;

	private EventEntity toEventEntity(NewEvent event, EventType type) {
		return EventEntity.builder()
				.id(null)
				.position(timeService.now())
				.taskId(event.getTaskId())
				.comment(event.getComment())
				.type(type)
				.build();
	}

	@POST
	@Path(ResourcePaths.NOTE_PATH)
	public void addUserNote(NewEvent event) {
		EventEntity eventEntity = toEventEntity(event, EventType.NOTE);
		persistenceService.saveEvent(eventEntity);
	}

	@POST
	@Path(ResourcePaths.SUBTASK_PATH)
	public void addSubtask(NewEvent event) {
		EventEntity eventEntity = toEventEntity(event, EventType.SUBTASK);
		persistenceService.saveEvent(eventEntity);
	}

	//Developers have been creating "note types" manually using [Subtask] and [Prediction] as prefixes in their comments.
	//Subtask events in particular I'm using to derive a "Subtask band" and collapse all the details of events/bands
	// that happen within a subtask, so you can "drill in" on one subtask at a time ford a complex IFM.

}
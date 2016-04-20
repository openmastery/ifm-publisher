package org.ideaflow.publisher.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.ideaflow.publisher.api.EditorActivity;
import org.ideaflow.publisher.api.ResourcePaths;
import org.springframework.stereotype.Component;

@Component
@Path(ResourcePaths.TASK_PATH + "/{taskId}" + ResourcePaths.ACTIVITY_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class ActivityResource {

	@POST
	@Path(ResourcePaths.EDITOR_PATH)
	public void addEditorActivity(@PathParam("taskId") String taskId, EditorActivity editorActivity) {

	}

	@POST
	@Path(ResourcePaths.IDLE_PATH)
	public void addIdleActivity(@PathParam("taskId") String taskId, String duration) {

	}

	@POST
	@Path(ResourcePaths.NOTE_PATH)
	public void addUserNote(@PathParam("taskId") String taskId, String message) {
		System.out.println("Add Note: " + taskId + ", " + message);
	}

	//Developers have been creating "note types" manually using [Subtask] and [Prediction] as prefixes in their comments.
	//Subtask events in particular I'm using to derive a "Subtask band" and collapse all the details of events/bands
	// that happen within a subtask, so you can "drill in" on one subtask at a time ford a complex IFM.

}

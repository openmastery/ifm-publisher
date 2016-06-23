package org.openmastery.publisher.client;

import org.openmastery.publisher.api.ResourcePaths;
import org.openmastery.publisher.api.activity.EditorActivity;
import org.openmastery.publisher.api.activity.NewEditorActivity;
import org.openmastery.publisher.api.activity.NewExternalActivity;
import org.openmastery.publisher.api.activity.NewIdleActivity;
import org.openmastery.rest.client.CrudClient;

public class ActivityClient extends CrudClient<EditorActivity, ActivityClient> {

	public ActivityClient(String baseUrl) {
		super(baseUrl, ResourcePaths.ACTIVITY_PATH, EditorActivity.class);
	}

	public void addEditorActivity(Long taskId, Long durationInSeconds, String filePath, boolean isModified) {
		NewEditorActivity activity = NewEditorActivity.builder()
					.taskId(taskId)
					.filePath(filePath)
					.isModified(isModified)
					.durationInSeconds(durationInSeconds)
					.build();

		crudClientRequest.path(ResourcePaths.EDITOR_PATH)
				.createWithPost(activity);
	}

	public void addIdleActivity(Long taskId, Long durationInSeconds, String comment, boolean auto) {
		NewIdleActivity activity = NewIdleActivity.builder()
				.taskId(taskId)
				.comment(comment)
				.auto(auto)
				.durationInSeconds(durationInSeconds)
				.build();

		crudClientRequest.path(ResourcePaths.IDLE_PATH)
				.createWithPost(activity);
	}

	public void addExternalActivity(Long taskId, Long durationInSeconds, String comment) {
		NewExternalActivity activity = NewExternalActivity.builder()
				.taskId(taskId)
				.durationInSeconds(durationInSeconds)
				.comment(comment)
				.build();

		crudClientRequest.path(ResourcePaths.EXTERNAL_PATH)
				.createWithPost(activity);
	}

}

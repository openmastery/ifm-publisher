/*
 * Copyright 2016 New Iron Group, Inc.
 *
 * Licensed under the GNU GENERAL PUBLIC LICENSE, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openmastery.publisher.metrics.subtask.calculator

import org.openmastery.publisher.api.event.ExecutionEvent
import org.openmastery.publisher.api.ideaflow.IdeaFlowBand
import org.openmastery.publisher.api.ideaflow.IdeaFlowStateType
import org.openmastery.publisher.api.ideaflow.IdeaFlowTimeline
import org.openmastery.publisher.api.metrics.Metric
import org.openmastery.publisher.api.metrics.MetricType

class AvgFeedbackLoopsCalculator  extends AbstractMetricsCalculator<Double> {

	AvgFeedbackLoopsCalculator() {
		super(MetricType.AVG_FEEDBACK_LOOPS)
	}

	/**
	 *
	 * What's the average number of execution events within a troubleshooting band?
	 *
	 * @param timeline for a subtask
	 * @return Metric<Double> the resulting metric value
	 */
	@Override
	Metric<Double> calculateMetrics(IdeaFlowTimeline timeline) {

		List<IdeaFlowBand> troubleshootingBands = timeline.ideaFlowBands.findAll() { IdeaFlowBand band ->
			band.type == IdeaFlowStateType.TROUBLESHOOTING
		}

		Double avgEventCount = 0;
		Long sampleCount = 0;

		troubleshootingBands.each { IdeaFlowBand troubleshootingBand ->
			Long relativeStart = troubleshootingBand.relativePositionInSeconds
			Long relativeEnd = troubleshootingBand.relativePositionInSeconds + troubleshootingBand.durationInSeconds

			int eventCount = countExecutionEventsInRange(timeline.executionEvents, relativeStart, relativeEnd)

			sampleCount++

			avgEventCount = (avgEventCount *(sampleCount - 1) + eventCount)/sampleCount
		}

		Metric<Double> metric = createMetric()
		metric.type = getMetricType()
		metric.value = avgEventCount
		return metric
	}

	int countExecutionEventsInRange(List<ExecutionEvent> executionEvents, Long relativeStart, Long relativeEnd ) {
		List<ExecutionEvent> eventsWithinRange = executionEvents.findAll() { ExecutionEvent event ->
			event.relativePositionInSeconds > relativeStart &&
					event.relativePositionInSeconds < relativeEnd
		}
		return eventsWithinRange.size()
	}


}
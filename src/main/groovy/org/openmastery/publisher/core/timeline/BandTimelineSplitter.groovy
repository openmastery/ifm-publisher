package org.openmastery.publisher.core.timeline

import org.openmastery.publisher.core.event.EventModel
import org.openmastery.publisher.api.event.EventType

class BandTimelineSplitter {

	List<BandTimelineSegment> splitTimelineSegment(BandTimelineSegment segment) {
		boolean hasSubtask = segment.events.find { it.type == EventType.SUBTASK }
		if (hasSubtask == false) {
			return [segment]
		}

		List timeBands = segment.getAllTimeBands()
		Collections.sort(timeBands, TimeBandComparator.INSTANCE);
		List<BandTimelineSegment> splitSegments = []
		BandTimelineSegment activeSegment = new BandTimelineSegment()
		activeSegment.id = segment.id
		activeSegment.description = segment.description

		List<EventModel> sortedEvents = segment.events.sort { EventModel event -> event.position }
		sortedEvents.each { EventModel event ->
			if ((event.type == EventType.SUBTASK) == false) {
				activeSegment.addEvent(event)
				return
			}

			while (timeBands.isEmpty() == false) {
				TimeBandModel timeBand = timeBands.remove(0)
				if (timeBand.endsOnOrBefore(event.position)) {
					activeSegment.addTimeBand(timeBand)
				} else {
					if (timeBand.start == event.position) {
						// pop the band back on the stack for processing by the next event
						timeBands.add(0, timeBand)
					} else {
						TimeBandModel leftBand = timeBand.splitAndReturnLeftSide(event.position)
						TimeBandModel rightBand = timeBand.splitAndReturnRightSide(event.position)
						activeSegment.addTimeBand(leftBand)
						timeBands.add(0, rightBand)
					}
					break
				}
			}

			if (activeSegment.ideaFlowBands || activeSegment.timeBandGroups) {
				splitSegments << activeSegment
				activeSegment = new BandTimelineSegment()
				activeSegment.addEvent(event)
				activeSegment.id = event.id
				activeSegment.description = event.comment
			}
		}

		if (timeBands) {
			timeBands.each { TimeBandModel timeBand ->
				activeSegment.addTimeBand(timeBand)
			}
			splitSegments << activeSegment
		}

		splitSegments
	}

}
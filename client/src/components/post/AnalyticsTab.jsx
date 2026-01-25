
import { Stack, Heading, Divider } from '@chakra-ui/react'
import AnalyticsPanel from '../AnalyticsPanel'
import EventCheckersPanel from '../../components/EventCheckersPanel'

export default function AnalyticsTab({ postId }) {
    return (
        <Stack spacing={5}>
            <Heading size="sm">Event analytics</Heading>

            <AnalyticsPanel
                postId={postId}
                days={7}
                showAttendance
            />

            <Divider />

            <Heading size="sm">Checkers management</Heading>
            <EventCheckersPanel postId={postId} />
        </Stack>
    )
}

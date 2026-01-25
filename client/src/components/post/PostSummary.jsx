
import { Heading, HStack, Badge, Tooltip } from '@chakra-ui/react'

export default function PostSummary({ post, availability }) {
    const seats = post?.capacity ?? null
    const available = availability?.available ?? null

    return (
        <>
            <Heading size="lg" mt={2}>{post.title}</Heading>

            <HStack mt={2} spacing={3}>
                {seats != null && (
                    <Tooltip label="Total seats">
                        <Badge variant="outline">{seats} seats</Badge>
                    </Tooltip>
                )}

                {seats != null && available != null && (
                    <Tooltip label="Available seats">
                        <Badge colorScheme={available === 0 ? 'red' : 'green'}>
                            {available} available
                        </Badge>
                    </Tooltip>
                )}

                {seats == null && (
                    <Tooltip label="No capacity limit">
                        <Badge variant="outline">unlimited</Badge>
                    </Tooltip>
                )}
            </HStack>
        </>
    )
}

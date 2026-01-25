
import { Stack, Text } from '@chakra-ui/react';

export default function PostMeta({ post }) {
    return (
        <Stack color="gray.600" mt={2}>
            <Text>📅 {post.startAt}</Text>
            {post.location && <Text>📍 {post.location}</Text>}
        </Stack>
    );
}

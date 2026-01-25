
import { HStack, Avatar, Heading, Badge } from '@chakra-ui/react';

export default function PostHeader({ post }) {
    return (
        <HStack spacing={3}>
            <Avatar name={`${post.userName} ${post.userLastName}`} />
            <Heading size="sm">
                {post.userName} {post.userLastName}
            </Heading>
            <Badge colorScheme="green">{post.status}</Badge>
        </HStack>
    );
}

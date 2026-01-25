
import { Text, Image, Stack } from '@chakra-ui/react';
import {api} from "../../api/api";

export default function PostDescription({ post }) {
    const imageUrl = `${api.defaults.baseURL}postimages/download/` + post.id;
    return (
        <Stack spacing={4}>
            <Text>{post.description}</Text>
            <Image src={imageUrl} maxH="md" objectFit="contain" />
        </Stack>
    );
}


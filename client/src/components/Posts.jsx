import { Center, VStack } from '@chakra-ui/react'
import React from 'react'
import PostCard from './PostCard'

export default function Posts({ posts }) {
    const imageUrl = process.env.REACT_APP_API + 'postimages/download/'

    return (
        <Center>
            <VStack marginTop={'50px'} spacing={5}>
                {posts.map(post => (
                    <PostCard
                        key={post.id}
                        // автор
                        userName={`${post.userName} ${post.userLastName}`}
                        userId={post.userId}

                        // базове
                        postId={post.id}
                        description={post.description}
                        postImage={imageUrl + post.id}

                        // івентні поля
                        title={post.title}
                        location={post.location}
                        startAt={post.startAt}
                        endAt={post.endAt}
                        capacity={post.capacity}
                        paid={post.paid}
                        price={post.price}
                        currency={post.currency}
                        salesStartAt={post.salesStartAt}
                        salesEndAt={post.salesEndAt}
                        status={post.status}
                    />
                ))}
            </VStack>
        </Center>
    )
}

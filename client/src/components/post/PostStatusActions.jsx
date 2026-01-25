
import { ButtonGroup, Button, useToast } from '@chakra-ui/react'
import { useMemo } from 'react'
import PostService from '../../services/PostService'

export default function PostStatusActions({ post, onStatusChange }) {
    const postService = useMemo(() => new PostService(), [])
    const toast = useToast()
    const token = localStorage.getItem('token')

    const changeStatus = async (status) => {
        try {
            const { data } = await postService.updateStatus(post.id, status, token)
            onStatusChange(data)
            toast({
                title: 'Status updated',
                status: 'success',
                duration: 2500,
                isClosable: true
            })
        } catch (e) {
            const msg = e?.response?.data?.message || e?.message || 'Failed to update status'
            toast({
                title: 'Error',
                description: msg,
                status: 'error',
                duration: 4000,
                isClosable: true
            })
        }
    }

    return (
        <ButtonGroup size="sm" variant="outline" mt={2}>
            <Button
                onClick={() => changeStatus('PUBLISHED')}
                isDisabled={post.status === 'PUBLISHED'}
                colorScheme="green"
            >
                Publish
            </Button>

            <Button
                onClick={() => changeStatus('DRAFT')}
                isDisabled={post.status === 'DRAFT'}
            >
                Move to Draft
            </Button>

            <Button
                onClick={() => changeStatus('CANCELLED')}
                isDisabled={post.status === 'CANCELLED'}
                colorScheme="red"
            >
                Cancel
            </Button>
        </ButtonGroup>
    )
}

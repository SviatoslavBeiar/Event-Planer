import { HStack, Button, useToast } from '@chakra-ui/react'
import { BiLike, BiShare } from 'react-icons/bi'
import { useCallback, useContext, useEffect, useMemo, useState } from 'react'
import AuthContext from '../../context/AuthContext'
import LikeService from '../../services/LikeService'
import CommentModal from '../CommentModal'

export default function PostSocial({ postId }) {
    const { user } = useContext(AuthContext)
    const likeService = useMemo(() => new LikeService(), [])
    const toast = useToast()

    const [likes, setLikes] = useState([])
    const [isLiked, setIsLiked] = useState(false)

    const refresh = useCallback(async () => {
        if (!user) return
        const token = localStorage.getItem('token')
        const [l1, l2] = await Promise.all([
            likeService.isLiked(user.id, postId, token),
            likeService.getLikesByPost(postId, token),
        ])
        setIsLiked(l1.data)
        setLikes(l2.data)
    }, [user, postId, likeService])

    useEffect(() => {
        refresh()
    }, [refresh])

    const toggleLike = async () => {
        const token = localStorage.getItem('token')
        if (isLiked) {
            await likeService.delete(user.id, postId, token)
        } else {
            await likeService.add(user.id, postId, token)
        }
        refresh()
    }

    const handleShare = async () => {
        const url = `${window.location.origin}/posts/${postId}`

        if (navigator.share) {
            try {
                await navigator.share({
                    title: 'Event',
                    text: 'Check this out',
                    url,
                })
                return
            } catch (e) {

                console.log('Share error:', e)
            }
        }

        // 2) Fallback: copy to clipboard
        try {
            await navigator.clipboard.writeText(url)
            toast({ title: 'Link copied', status: 'success', duration: 2000 })
        } catch (e) {
            // 3) Last resort
            window.prompt('Copy this link:', url)
        }
    }

    return (
        <HStack mt={4} spacing={3}>
            <Button onClick={toggleLike} leftIcon={<BiLike />} variant={isLiked ? 'solid' : 'ghost'}>
                {likes.length}
            </Button>

            <CommentModal postId={postId} />

            <Button onClick={handleShare} leftIcon={<BiShare />} variant="ghost">
                Share
            </Button>
        </HStack>
    )
}

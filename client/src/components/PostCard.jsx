import React, { useCallback, useContext, useEffect, useMemo, useState } from 'react'
import {
    Flex, Card, CardHeader, CardBody, CardFooter, Button, Avatar, Box,
    Heading, Text, Image, LinkBox, LinkOverlay, HStack, Badge, Stack, Tooltip
} from '@chakra-ui/react'
import { Link } from 'react-router-dom'
import { BiLike, BiShare } from 'react-icons/bi'
import { FiMapPin, FiCalendar, FiClock } from 'react-icons/fi'
import AuthContext from '../context/AuthContext'
import LikeService from '../services/LikeService'
import CommentModal from './CommentModal'

function fmtDate(dt) {
    if (!dt) return ''
    try {
        return new Intl.DateTimeFormat(undefined, {
            year: 'numeric', month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit'
        }).format(new Date(dt))
    } catch { return String(dt) }
}
function fmtPrice(p, cur) {
    if (p == null) return ''
    try {
        return new Intl.NumberFormat(undefined, { style: 'currency', currency: cur || 'USD' }).format(p)
    } catch { return `${p} ${cur || ''}` }
}

export default function PostCard({
                                     userName, userImage, description, postImage, postId, userId,
                                     title, location, startAt, endAt, capacity, paid, price, currency,
                                     salesStartAt, salesEndAt, status
                                 }) {
    const { user } = useContext(AuthContext)
    const [isLiked, setIsLiked] = useState(false)
    const [likes, setLikes] = useState([])
    const likeService = useMemo(() => new LikeService(), [])

    const refresh = useCallback(async () => {
        try {
            const token = localStorage.getItem('token')
            const [likedRes, likesRes] = await Promise.all([
                likeService.isLiked(user.id, postId, token),
                likeService.getLikesByPost(postId, token),
            ])
            setIsLiked(likedRes.data)
            setLikes(likesRes.data)
        } catch (e) { console.log(e) }
    }, [likeService, user.id, postId])

    useEffect(() => { refresh() }, [refresh])

    const handleLike = async () => {
        try { await likeService.add(user.id, postId, localStorage.getItem('token')); await refresh() } catch {}
    }
    const handleUnlike = async () => {
        try { await likeService.delete(user.id, postId, localStorage.getItem('token')); await refresh() } catch {}
    }

    const isFree = paid === false || (paid === true && (price === 0 || price === null))
    const priceBadge = isFree ? <Badge colorScheme="green">Free</Badge>
        : <Badge colorScheme="purple">{fmtPrice(price, currency)}</Badge>

    return (
        <Card as={LinkBox} maxW='lg' w="lg">
            <CardHeader as={Link} to={`/profile/${userId}`}>
                <Flex spacing='4'>
                    <Flex flex='1' gap='4' alignItems='center' flexWrap='wrap'>
                        <Avatar name={userName} src={userImage} />
                        <Box>
                            <Heading size='sm'>{userName}</Heading>
                            {status && <Badge ml={2} colorScheme={
                                status === 'PUBLISHED' ? 'green'
                                    : status === 'DRAFT' ? 'yellow'
                                        : status === 'CANCELLED' ? 'red'
                                            : 'gray'
                            }>{status}</Badge>}
                        </Box>
                    </Flex>
                </Flex>
            </CardHeader>

            <CardBody>
                <Stack spacing={2}>
                    {title && (
                        <Heading size="md">
                            <LinkOverlay as={Link} to={`/post/${postId}`}>{title}</LinkOverlay>
                        </Heading>
                    )}

                    {(startAt || endAt || location) && (
                        <HStack spacing={4} color="gray.600" fontSize="sm">
                            {(startAt || endAt) && (
                                <HStack>
                                    <FiCalendar />
                                    <Text>
                                        {fmtDate(startAt)}{endAt ? ` — ${fmtDate(endAt)}` : ''}
                                    </Text>
                                </HStack>
                            )}
                            {location && (
                                <HStack>
                                    <FiMapPin />
                                    <Text>{location}</Text>
                                </HStack>
                            )}
                        </HStack>
                    )}

                    {(salesStartAt || salesEndAt) && (
                        <HStack spacing={3} fontSize="sm" color="gray.600">
                            <FiClock />
                            <Text>Sales: {fmtDate(salesStartAt)}{salesEndAt ? ` — ${fmtDate(salesEndAt)}` : ''}</Text>
                        </HStack>
                    )}

                    <HStack spacing={3}>
                        {priceBadge}
                        {capacity != null && (
                            <Tooltip label="Capacity">
                                <Badge variant="outline">{capacity} seats</Badge>
                            </Tooltip>
                        )}
                    </HStack>

                    {description && <Text>{description}</Text>}
                </Stack>
            </CardBody>

            {postImage && (
                <Box px={6} pb={2}>
                    <Link to={`/post/${postId}`}>
                        <Image maxW='md' maxH='sm' objectFit='contain' src={postImage} fallback={null} />
                    </Link>
                </Box>
            )}

            <CardFooter justify='space-between' flexWrap='wrap' sx={{'& > button': { minW: '136px' }}}>
                {isLiked ? (
                    <Button onClick={handleUnlike} flex='1' colorScheme='pink' leftIcon={<BiLike />}>
                        Like {likes.length}
                    </Button>
                ) : (
                    <Button onClick={handleLike} flex='1' variant='ghost' leftIcon={<BiLike />}>
                        Like {likes.length}
                    </Button>
                )}
                <CommentModal postId={postId} />
                <Button flex='1' variant='ghost' leftIcon={<BiShare />}>Share</Button>
            </CardFooter>
        </Card>
    )
}


// src/pages/PostDetails.jsx
import { useEffect, useMemo, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import {
    Box, Center, Heading, Image, Spinner, Stack, Text, HStack, Avatar,
    Button, Badge, Divider, Tooltip, Code, useToast, Wrap, WrapItem
} from '@chakra-ui/react'
import { FiMapPin, FiCalendar, FiClock } from 'react-icons/fi'
import { CopyIcon } from '@chakra-ui/icons'
import PostService from '../services/PostService'
import TicketService from '../services/TicketService'
import CommentModal from '../components/CommentModal'

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

export default function PostDetails() {
    const { postId } = useParams()
    const toast = useToast()

    const postService = useMemo(() => new PostService(), [])
    const ticketService = useMemo(() => new TicketService(), [])

    const [post, setPost] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    const [myTicket, setMyTicket] = useState(null)
    const [ticketLoading, setTicketLoading] = useState(false)
    const [ticketError, setTicketError] = useState('')

    const imageUrl = process.env.REACT_APP_API + 'postimages/download/' + postId

    // Load post
    useEffect(() => {
        let mounted = true
        ;(async () => {
            try {
                const res = await postService.getById(postId, localStorage.getItem('token'))
                if (mounted) setPost(res.data)
            } catch (e) {
                setError(e?.message || 'Failed to load post')
            } finally {
                if (mounted) setLoading(false)
            }
        })()
        return () => { mounted = false }
    }, [postId, postService])

    // Load my ticket (if any)
    useEffect(() => {
        let mounted = true
        ;(async () => {
            try {
                setTicketLoading(true)
                const res = await ticketService.getMy(Number(postId), localStorage.getItem('token'))
                if (mounted) setMyTicket(res.data)
            } catch (e) {
                // 404 = not registered yet; ignore
                if (e?.response?.status !== 404) setTicketError(e?.message || 'Ticket error')
            } finally {
                if (mounted) setTicketLoading(false)
            }
        })()
        return () => { mounted = false }
    }, [postId, ticketService])

    const handleRegister = async () => {
        try {
            setTicketLoading(true)
            const res = await ticketService.register(Number(postId), localStorage.getItem('token'))
            setMyTicket(res.data)
            toast({ title: 'Registered', status: 'success', duration: 4000, isClosable: true })
        } catch (e) {
            const msg = e?.response?.data?.message || e?.response?.data || e?.message || 'Registration failed'
            toast({ title: 'Registration failed', description: msg, status: 'error', duration: 6000, isClosable: true })
        } finally {
            setTicketLoading(false)
        }
    }

    const copyCode = async (code) => {
        try {
            await navigator.clipboard.writeText(code)
            toast({ title: 'Ticket code copied', status: 'success', duration: 2500, isClosable: true })
        } catch {
            toast({ title: 'Copy failed', status: 'error', duration: 2500, isClosable: true })
        }
    }

    if (loading) return <Center h="50vh"><Spinner size="lg" /></Center>
    if (error || !post) return <Center h="50vh"><Heading size="md" color="red.500">{error || 'Post not found'}</Heading></Center>

    const isFree = post.paid === false || (post.paid === true && (!post.price || Number(post.price) === 0))
    const isPublished = post.status === 'PUBLISHED'

    return (
        <Center mt={8}>
            <Stack spacing={4} w="lg">
                {/* Автор + статус */}
                <HStack as={Link} to={`/profile/${post.userId}`} spacing={3}>
                    <Avatar name={`${post.userName} ${post.userLastName}`} />
                    <Heading size="sm">{post.userName} {post.userLastName}</Heading>
                    {post.status && (
                        <Badge colorScheme={
                            post.status === 'PUBLISHED' ? 'green'
                                : post.status === 'DRAFT' ? 'yellow'
                                    : post.status === 'CANCELLED' ? 'red'
                                        : 'gray'
                        }>
                            {post.status}
                        </Badge>
                    )}
                </HStack>

                {/* Заголовок івенту */}
                {post.title && <Heading size="lg">{post.title}</Heading>}

                {/* Дата/час + локація */}
                {(post.startAt || post.endAt || post.location) && (
                    <HStack spacing={5} color="gray.600">
                        {(post.startAt || post.endAt) && (
                            <HStack>
                                <FiCalendar />
                                <Text>{fmtDate(post.startAt)}{post.endAt ? ` — ${fmtDate(post.endAt)}` : ''}</Text>
                            </HStack>
                        )}
                        {post.location && (
                            <HStack>
                                <FiMapPin />
                                <Text>{post.location}</Text>
                            </HStack>
                        )}
                    </HStack>
                )}

                {/* Вікно продажів */}
                {(post.salesStartAt || post.salesEndAt) && (
                    <HStack color="gray.600">
                        <FiClock />
                        <Text>Sales: {fmtDate(post.salesStartAt)}{post.salesEndAt ? ` — ${fmtDate(post.salesEndAt)}` : ''}</Text>
                    </HStack>
                )}

                {/* Ціна/Безкоштовно + місткість */}
                <HStack>
                    {isFree ? <Badge colorScheme="green">Free</Badge>
                        : <Badge colorScheme="purple">{fmtPrice(post.price, post.currency)}</Badge>}
                    {post.capacity != null && (
                        <Tooltip label="Capacity">
                            <Badge variant="outline">{post.capacity} seats</Badge>
                        </Tooltip>
                    )}
                </HStack>

                {/* Опис */}
                {post.description && <Text>{post.description}</Text>}

                {/* Картинка */}
                <Box>
                    <Image src={imageUrl} alt="Post image" objectFit="contain" maxH="md" fallbackSrc="" />
                </Box>

                <Divider />

                {/* НИЗ (вертикально):
            1) Кнопка/статус квитка
            2) Кнопка коментарів
            3) Back to Home (внизу) */}
                <Stack spacing={4} w="100%">
                    {/* 1) Реєстрація / Статус */}
                    {myTicket ? (
                        <Wrap align="center">
                            <WrapItem><Badge colorScheme="green">REGISTERED</Badge></WrapItem>
                            <WrapItem>
                                <Text fontSize="sm">
                                    Ticket code:{' '}
                                    <Code fontSize="sm" wordBreak="break-all">
                                        {myTicket?.code}
                                    </Code>
                                </Text>
                            </WrapItem>
                            <WrapItem>
                                <Button
                                    size="sm"
                                    leftIcon={<CopyIcon />}
                                    onClick={() => copyCode(myTicket?.code || '')}
                                >
                                    Copy
                                </Button>
                            </WrapItem>
                        </Wrap>
                    ) : (
                        <Button
                            isLoading={ticketLoading}
                            onClick={handleRegister}
                            colorScheme="pink"
                            isDisabled={!isPublished}
                            alignSelf="flex-start"
                        >
                            {isPublished ? 'Register' : 'Registration disabled'}
                        </Button>
                    )}

                    {/* 2) Коментарі */}
                    <CommentModal postId={post.id} />

                    {/* 3) Back to Home */}
                    <Divider />
                    <Button as={Link} to="/home" variant="ghost" alignSelf="flex-end">
                        Back to Home
                    </Button>
                </Stack>

                {ticketError && <Text color="red.400" fontSize="sm">{ticketError}</Text>}
            </Stack>
        </Center>
    )
}

// src/pages/PostDetails.jsx
import { useEffect, useMemo, useState, useContext } from 'react'
import { useParams, Link } from 'react-router-dom'
import {
    Box, Center, Heading, Image, Spinner, Stack, Text, HStack, Avatar,
    Button, Badge, Divider, Tooltip, Code, useToast, Wrap, WrapItem, ButtonGroup
} from '@chakra-ui/react'
import { FiMapPin, FiCalendar, FiClock } from 'react-icons/fi'
import { CopyIcon } from '@chakra-ui/icons'
import PostService from '../services/PostService'
import TicketService from '../services/TicketService'
import EventCheckerService from '../services/EventCheckerService'
import CommentModal from '../components/CommentModal'
import AuthContext from '../context/AuthContext'
import EventCheckersPanel from './EventCheckersPanel'
import QrTicketCard from '../components/QrTicketCard';
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
    const { user } = useContext(AuthContext)

    const postService = useMemo(() => new PostService(), [])
    const ticketService = useMemo(() => new TicketService(), [])
    const checkerService = useMemo(() => new EventCheckerService(), [])

    const [post, setPost] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    const [myTicket, setMyTicket] = useState(null)
    const [ticketLoading, setTicketLoading] = useState(false)
    const [ticketError, setTicketError] = useState('')

    // availability / capacity
    const [availability, setAvailability] = useState(null)
    const [availLoading, setAvailLoading] = useState(false)

    // am i checker
    const [isChecker, setIsChecker] = useState(false)

    const imageUrl = process.env.REACT_APP_API + 'postimages/download/' + postId
    const token = localStorage.getItem('token')

    // Load post
    useEffect(() => {
        let mounted = true
        ;(async () => {
            try {
                const res = await postService.getById(postId, token)
                if (mounted) setPost(res.data)
            } catch (e) {
                setError(e?.message || 'Failed to load post')
            } finally {
                if (mounted) setLoading(false)
            }
        })()
        return () => { mounted = false }
    }, [postId, postService, token])

    // Load my ticket (if any)
    useEffect(() => {
        let mounted = true
        ;(async () => {
            try {
                setTicketLoading(true)
                const res = await ticketService.getMy(Number(postId), token)
                if (mounted) setMyTicket(res.data)
            } catch (e) {
                if (e?.response?.status !== 404) setTicketError(e?.message || 'Ticket error')
            } finally {
                if (mounted) setTicketLoading(false)
            }
        })()
        return () => { mounted = false }
    }, [postId, ticketService, token])

    // Load availability (sold/capacity/remaining)
    useEffect(() => {
        let mounted = true
        ;(async () => {
            try {
                setAvailLoading(true)
                const { data } = await ticketService.getAvailability(Number(postId), token)
                if (mounted) setAvailability(data)
            } catch {
                // ignore; UI просто не покаже залишок
            } finally {
                if (mounted) setAvailLoading(false)
            }
        })()
        return () => { mounted = false }
    }, [postId, ticketService, token])

    // Am I checker for this event?
    useEffect(() => {
        let mounted = true
        if (!postId || !user) return
            ;(async () => {
            try {
                const res = await checkerService.amIChecker(Number(postId), token)
                if (mounted) setIsChecker(Boolean(res.data))
            } catch {
                if (mounted) setIsChecker(false)
            }
        })()
        return () => { mounted = false }
    }, [postId, user, checkerService, token])

    if (loading) return <Center h="50vh"><Spinner size="lg" /></Center>
    if (error || !post) return <Center h="50vh"><Heading size="md" color="red.500">{error || 'Post not found'}</Heading></Center>

    const isFree = post.paid === false || (post.paid === true && (!post.price || Number(post.price) === 0))
    const isPublished = post.status === 'PUBLISHED'

    // author
    const isMyEvent = user?.role === 'ORGANIZER' && Number(user?.id) === Number(post?.userId)

    const canManageCheckers = isMyEvent
    const canOpenVerifier = isMyEvent || isChecker

    // sold out?
    const isSoldOut = post.capacity != null && availability?.full === true

    const handleRegister = async () => {
        try {
            setTicketLoading(true)
            const res = await ticketService.register(Number(postId), token)
            setMyTicket(res.data)
            toast({ title: 'Registered', status: 'success', duration: 4000, isClosable: true })
            // refresh availability
            try {
                const { data } = await ticketService.getAvailability(Number(postId), token)
                setAvailability(data)
            } catch {}
        } catch (e) {
            const msg = e?.response?.data?.message || e?.response?.data || e?.message || 'Registration failed'
            toast({ title: 'Registration failed', description: msg, status: 'error', duration: 6000, isClosable: true })
            // handle full locally
            if (String(msg).toLowerCase().includes('event is full')) {
                setAvailability((prev) => ({ ...(prev || {}), full: true, remaining: 0, capacity: post.capacity }))
            }
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

    // Organizer: change post status
    const changeStatus = async (status) => {
        try {
            const { data } = await postService.updateStatus(Number(postId), status, token)
            setPost(data)
            toast({ title: 'Status updated', status: 'success', duration: 2500, isClosable: true })
        } catch (e) {
            const msg = e?.response?.data?.message || e?.message || 'Failed to update status'
            toast({ title: 'Error', description: msg, status: 'error', duration: 4000, isClosable: true })
        }
    }

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

                {/* Заголовок */}
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

                {/* Ціна / Місткість / Доступність */}
                <HStack>
                    {isFree ? <Badge colorScheme="green">Free</Badge>
                        : <Badge colorScheme="purple">{fmtPrice(post.price, post.currency)}</Badge>}
                    {post.capacity != null && (
                        <Tooltip label="Capacity">
                            <Badge variant="outline">{post.capacity} seats</Badge>
                        </Tooltip>
                    )}
                    {post.capacity != null && availability && (
                        availability.full ? (
                            <Badge colorScheme="red">Sold out</Badge>
                        ) : (
                            <Badge variant="subtle">
                                {availLoading ? '...' : `${availability.remaining ?? 0} left`}
                            </Badge>
                        )
                    )}
                </HStack>

                {/* Опис */}
                {post.description && <Text>{post.description}</Text>}

                {/* Картинка */}
                <Box>
                    <Image src={imageUrl} alt="Post image" objectFit="contain" maxH="md" fallbackSrc="" />
                </Box>

                {/* Organizer-only: status actions */}
                {isMyEvent && (
                    <ButtonGroup size="sm" variant="outline">
                        <Button onClick={() => changeStatus('PUBLISHED')} isDisabled={post.status === 'PUBLISHED'}>
                            Publish
                        </Button>
                        <Button onClick={() => changeStatus('DRAFT')} isDisabled={post.status === 'DRAFT'}>
                            Move to Draft
                        </Button>
                        <Button onClick={() => changeStatus('CANCELLED')} colorScheme="red" isDisabled={post.status === 'CANCELLED'}>
                            Cancel
                        </Button>
                    </ButtonGroup>
                )}

                <Divider />

                {/* Низ: 1) квиток 2) коментарі 3) Back */}
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
                            isDisabled={!isPublished || isSoldOut}
                            alignSelf="flex-start"
                        >
                            {!isPublished ? 'Registration disabled' : (isSoldOut ? 'Sold out' : 'Register')}
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

                {/* Керування чекерами — тільки для автора-організатора */}
                {isMyEvent && <EventCheckersPanel postId={post.id} />}

                {myTicket && (
                    <>
                        {/* ...тут твій блок REGISTERED і кнопка Copy... */}
                        <QrTicketCard postId={post.id} ticketCode={myTicket?.code} />
                    </>
                )}

                {/* Відкрити верифікатор — автор або призначений checker */}
                {(isMyEvent || isChecker) && (
                    <Button as={Link} to={`/verify/${post.id}`} variant="outline">
                        Open Verifier
                    </Button>
                )}


            </Stack>
        </Center>
    )
}

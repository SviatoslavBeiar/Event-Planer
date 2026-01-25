import { useEffect, useMemo, useState, useContext } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
    Box,
    Center,
    Spinner,
    Tabs,
    TabList,
    TabPanels,
    Tab,
    TabPanel,
    Card,
    Button,
    HStack,
    useColorModeValue,
} from '@chakra-ui/react'

import AuthContext from '../context/AuthContext'
import PostService from '../services/PostService'
import TicketService from '../services/TicketService'
import EventCheckerService from '../services/EventCheckerService'

import PostHeader from '../components/post/PostHeader'
import PostSummary from '../components/post/PostSummary'
import PostMeta from '../components/post/PostMeta'
import PostDescription from '../components/post/PostDescription'
import PostSocial from '../components/post/PostSocial'

import TicketTab from '../components/post/TicketTab'
import CheckerTab from '../components/post/CheckerTab'
import AnalyticsTab from '../components/post/AnalyticsTab'
import PostStatusActions from '../components/post/PostStatusActions'

function PageShell({ children }) {
    const pageBg = useColorModeValue(
        'linear(to-br, purple.50, pink.50, blue.50)',
        'linear(to-br, gray.950, purple.950, blue.950)'
    )

    return (
        <Box minH="100vh" bgGradient={pageBg} py={{ base: 6, md: 10 }} px={4} position="relative" overflow="hidden">
            <Box
                position="absolute"
                top="-160px"
                left="-160px"
                w="420px"
                h="420px"
                bg={useColorModeValue('purple.300', 'purple.500')}
                opacity={useColorModeValue(0.20, 0.12)}
                filter="blur(110px)"
                borderRadius="full"
                pointerEvents="none"
            />
            <Box
                position="absolute"
                bottom="-180px"
                right="-180px"
                w="480px"
                h="480px"
                bg={useColorModeValue('pink.300', 'pink.500')}
                opacity={useColorModeValue(0.18, 0.10)}
                filter="blur(120px)"
                borderRadius="full"
                pointerEvents="none"
            />

            <Box position="relative" zIndex={1}>
                {children}
            </Box>
        </Box>
    )
}

function GlassCard({ children }) {
    const cardBg = useColorModeValue('whiteAlpha.800', 'blackAlpha.600')
    const cardBorder = useColorModeValue('blackAlpha.200', 'whiteAlpha.200')

    return (
        <Card
            w="full"
            maxW="lg"
            p={{ base: 4, md: 5 }}
            borderRadius="2xl"
            bg={cardBg}
            border="1px solid"
            borderColor={cardBorder}
            boxShadow="xl"
            backdropFilter="blur(12px)"
        >
            {children}
        </Card>
    )
}

function LoadingState() {
    return (
        <Center h="60vh">
            <Spinner />
        </Center>
    )
}

export default function PostDetailsPage() {
    const { postId } = useParams()
    const navigate = useNavigate()
    const { user } = useContext(AuthContext)

    const postService = useMemo(() => new PostService(), [])
    const ticketService = useMemo(() => new TicketService(), [])
    const checkerService = useMemo(() => new EventCheckerService(), [])
    const token = localStorage.getItem('token')

    const [post, setPost] = useState(null)
    const [myTicket, setMyTicket] = useState(null)
    const [availability, setAvailability] = useState(null) // ✅
    const [isChecker, setIsChecker] = useState(false)
    const [loading, setLoading] = useState(true)

    const refreshAvailability = async () => {
        try {
            const a = await ticketService.getAvailability(postId, token)
            setAvailability(a.data)
        } catch {
            // ignore
        }
    }

    useEffect(() => {
        let mounted = true
        ;(async () => {
            try {
                const p = await postService.getById(postId, token)
                if (mounted) setPost(p.data)

                await refreshAvailability()

                try {
                    const t = await ticketService.getMy(postId, token)
                    if (mounted) setMyTicket(t.data)
                } catch {}

                if (user) {
                    const c = await checkerService.amIChecker(postId, token)
                    if (mounted) setIsChecker(Boolean(c.data))
                }
            } finally {
                if (mounted) setLoading(false)
            }
        })()

        return () => {
            mounted = false
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [postId, user])

    if (loading) {
        return (
            <PageShell>
                <LoadingState />
            </PageShell>
        )
    }

    if (!post) return null

    const isOrganizer =
        user?.role === 'ORGANIZER' &&
        Number(user.id) === Number(post.userId)

    const handleRegistered = async (ticket) => {
        setMyTicket(ticket)
        await refreshAvailability()
    }

    return (
        <PageShell>
            <Center>
                <GlassCard>
                    {/* ✅ КНОПКА НАЗАД ДО HOME */}
                    <HStack justify="flex-start" mb={3}>
                        <Button
                            size="sm"
                            variant="outline"
                            onClick={() => navigate('/home')}
                        >
                            ← Back to Home
                        </Button>
                    </HStack>

                    <PostHeader post={post} />

                    <PostSummary post={post} availability={availability} />

                    {isOrganizer && (
                        <PostStatusActions post={post} onStatusChange={setPost} />
                    )}

                    <PostMeta post={post} />
                    <PostDescription post={post} />
                    <PostSocial postId={post.id} />

                    <Tabs mt={6} variant="soft-rounded" colorScheme="purple">
                        <TabList>
                            <Tab>Ticket</Tab>
                            {(isChecker || isOrganizer) && <Tab>Verifier</Tab>}
                            {isOrganizer && <Tab>Analytics</Tab>}
                        </TabList>

                        <TabPanels>
                            <TabPanel>
                                <TicketTab
                                    post={post}
                                    myTicket={myTicket}
                                    onRegistered={handleRegistered} // ✅ важливо
                                />
                            </TabPanel>

                            {(isChecker || isOrganizer) && (
                                <TabPanel>
                                    <CheckerTab postId={post.id} />
                                </TabPanel>
                            )}

                            {isOrganizer && (
                                <TabPanel>
                                    <AnalyticsTab postId={post.id} />
                                </TabPanel>
                            )}
                        </TabPanels>
                    </Tabs>
                </GlassCard>
            </Center>
        </PageShell>
    )
}

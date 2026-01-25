import {
    Center, Heading, HStack, Image, VStack,
    Tabs, TabList, TabPanels, Tab, TabPanel, Spinner, Text
} from '@chakra-ui/react'
import { useCallback, useContext, useEffect, useMemo, useState } from 'react'
import Nav from '../components/Nav'
import Posts from '../components/Posts'
import ProfileCard from '../components/ProfileCard'
import AuthContext from '../context/AuthContext'
import PostService from '../services/PostService'
import svg from '../svgs/undraw_no_data_re_kwbl.svg'

function Home() {
    const { user } = useContext(AuthContext)
    const token = useMemo(() => localStorage.getItem('token'), [])
    const [tabIndex, setTabIndex] = useState(0) // 0 = Підписки, 1 = Всі пости

    const [posts, setPosts] = useState([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')

    const fetchPosts = useCallback(async () => {
        if (!token) {
            setPosts([])
            setError('No token')
            return
        }
        setLoading(true)
        setError('')
        const postService = new PostService()

        try {
            let res
            if (tabIndex === 0) {
                if (!user?.id) {
                    setPosts([])
                    setLoading(false)
                    return
                }
                res = await postService.getAllByUserFollowing(user.id, token)
            } else {
                res = await postService.getAll(token)
            }

            // axios: дані в res.data; іноді API загортає payload у res.data.data
            const payload = Array.isArray(res?.data) ? res.data : (res?.data?.data ?? [])
            setPosts(payload)
        } catch (e) {
            setError(e?.message || 'Failed to load posts')
            setPosts([])
        } finally {
            setLoading(false)
        }
    }, [tabIndex, token, user?.id])

    useEffect(() => {
        fetchPosts()
    }, [fetchPosts])

    const EmptyState = (
        <Center>
            <VStack h={'60vh'} alignItems={'center'} justifyContent={'center'} spacing={6}>
                <Heading size="md">
                    {tabIndex === 0 ? 'There are no posts from users you are following.' : 'No posts yet.'}
                </Heading>
                <Image src={svg} h={'40vh'} alt="No data" />
                {tabIndex === 0 && (
                    <Text color="gray.500">Follow someone or open the “All Posts” tab.</Text>
                )}
            </VStack>
        </Center>
    )

    const LoadingState = (
        <Center h="40vh">
            <HStack>
                <Spinner size="lg" />
                <Text>Loading…</Text>
            </HStack>
        </Center>
    )

    const ErrorState = (
        <Center h="40vh">
            <VStack>
                <Heading size="sm" color="red.500">Error</Heading>
                <Text color="red.400">{error}</Text>
            </VStack>
        </Center>
    )

    return (
        <>
            <Nav />
            <ProfileCard userName={user?.fullName || ''} />

            <Tabs index={tabIndex} onChange={setTabIndex} variant="enclosed" isFitted>
                <TabList>
                    <Tab>Subscriptions</Tab>
                    <Tab>All posts</Tab>
                </TabList>

                <TabPanels>
                    <TabPanel px={0}>
                        {loading ? LoadingState : error ? ErrorState : posts.length === 0 ? EmptyState : <Posts posts={posts} />}
                    </TabPanel>
                    <TabPanel px={0}>
                        {loading ? LoadingState : error ? ErrorState : posts.length === 0 ? EmptyState : <Posts posts={posts} />}
                    </TabPanel>
                </TabPanels>
            </Tabs>
        </>
    )
}

export default Home

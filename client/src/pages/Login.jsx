// src/pages/Login.jsx
import React, { useContext, useMemo, useState } from 'react'
import {
    Button,
    Container,
    Divider,
    Flex,
    FormControl,
    FormErrorMessage,
    FormLabel,
    Heading,
    HStack,
    Icon,
    Image,
    Input,
    InputGroup,
    InputRightElement,
    Stack,
    Text,
    useBreakpointValue,
    useColorModeValue,
    useToast,
    VStack,
    Box,
} from '@chakra-ui/react'
import { ViewIcon, ViewOffIcon } from '@chakra-ui/icons'
import { FiMail, FiLock } from 'react-icons/fi'
import { useFormik } from 'formik'
import AuthContext from '../context/AuthContext'
import AuthService from '../services/AuthService'
import { useNavigate, Link } from 'react-router-dom'
import svg from '../svgs/main.svg'

// --- helpers -------------------------------------------------

function pickStatus(e) {
    if (e?.response?.status) return e.response.status
    if (e?.status) return e.status
    const m = e?.message || ''
    const match = m.match(/status code\s+(\d+)/i)
    return match ? Number(match[1]) : undefined
}

function pickRaw(e) {
    if (e?.response?.data !== undefined) return e.response.data
    if (e?.data !== undefined) return e.data
    if (typeof e === 'string') return e
    return undefined
}

function normalizeCode(raw) {
    if (typeof raw === 'string') return raw.replace(/^"|"$/g, '').trim()
    if (raw && typeof raw === 'object') {
        return (raw.code || raw.message || raw.error || '').toString().trim()
    }
    return ''
}

// ------------------------------------------------------------

export default function Login() {
    const { login } = useContext(AuthContext)
    const authService = useMemo(() => new AuthService(), [])
    const navigate = useNavigate()
    const toast = useToast()

    const [showPassword, setShowPassword] = useState(false)

    const cardBg = useColorModeValue('white', 'gray.800')
    const cardBorder = useColorModeValue('blackAlpha.200', 'whiteAlpha.200')
    const subtleText = useColorModeValue('gray.600', 'gray.300')

    const formik = useFormik({
        initialValues: { email: '', password: '' },

        onSubmit: async (values, { setSubmitting, setErrors, setFieldError }) => {
            const toastId = 'login-toast'

            try {
                const { data } = await authService.login(values) // token
                login(data)

                toast.close(toastId)
                toast({
                    id: toastId,
                    title: 'Logged in ✅',
                    description: 'Welcome back!',
                    status: 'success',
                    duration: 6000,
                    isClosable: true,
                })

                navigate('/home')
            } catch (e) {
                const status = pickStatus(e)
                const raw = pickRaw(e)
                const code = normalizeCode(raw)

                toast.close(toastId)

                // 403 — email not verified
                if (status === 403 || code === 'EMAIL_NOT_VERIFIED') {
                    toast({
                        id: toastId,
                        title: 'Email not verified ✉️',
                        description:
                            'Please confirm your email from the message we sent you, then try logging in again.',
                        status: 'warning',
                        duration: 9000,
                        isClosable: true,
                    })
                    setFieldError('email', 'Email is not verified')
                    return
                }

                // 401 — invalid credentials
                if (status === 401 || code === 'INVALID_CREDENTIALS') {
                    toast({
                        id: toastId,
                        title: 'Wrong email or password',
                        description: 'Please check your credentials and try again.',
                        status: 'error',
                        duration: 9000,
                        isClosable: true,
                    })
                    setErrors({ password: 'Wrong email or password' })
                    return
                }

                // no response -> backend down / CORS / network
                if (!status) {
                    toast({
                        id: toastId,
                        title: 'Server unavailable',
                        description:
                            'Cannot reach the backend. Make sure Spring is running and CORS is configured correctly.',
                        status: 'error',
                        duration: 9000,
                        isClosable: true,
                    })
                    return
                }

                // fallback other statuses
                toast({
                    id: toastId,
                    title: 'Login error',
                    description: 'Something went wrong. Please try again.',
                    status: 'error',
                    duration: 9000,
                    isClosable: true,
                })
            } finally {
                setSubmitting(false)
            }
        },
    })

    return (
        <Stack direction="row" spacing={0} minH="100vh">
            {/* LEFT HERO */}
            <Flex
                alignItems="center"
                justifyContent="center"
                width={{ base: 0, md: '100%', lg: '100%' }}
            >
                <VStack p={10} spacing={6}>
                    <Stack spacing={6} w="full" maxW="lg">
                        <Heading fontSize={{ base: '0', md: '5xl', lg: '6xl' }}>
                            <Text
                                as="span"
                                position="relative"
                                _after={{
                                    content: "''",
                                    width: 'full',
                                    height: useBreakpointValue({ base: '20%', md: '30%' }),
                                    position: 'absolute',
                                    bottom: 1,
                                    left: 0,
                                    bg: 'pink.500',
                                    zIndex: -1,
                                }}
                            >
                                Event Planer
                            </Text>
                            <br />
                            <Text color="pink.500" as="span">
                                Spring && React
                            </Text>
                        </Heading>

                        <Text color={subtleText} fontSize="md" maxW="md">
                            Log in to manage your events and access your account.
                        </Text>
                    </Stack>

                    <Image src={svg} alt="illustration" />
                </VStack>
            </Flex>

            {/* RIGHT FORM */}
            <Flex justifyContent="center" alignItems="center" width="100%" py={{ base: 10, md: 0 }}>
                <Container maxW="lg">
                    <VStack
                        as="form"
                        onSubmit={formik.handleSubmit}
                        bg={cardBg}
                        border="1px solid"
                        borderColor={cardBorder}
                        borderRadius="2xl"
                        boxShadow="2xl"
                        p={{ base: 6, md: 10 }}
                        spacing={6}
                        align="stretch"
                    >
                        <Box>
                            <Heading size="lg">Login</Heading>
                            <Text mt={2} color={subtleText} fontSize="sm">
                                Enter your email and password to continue.
                            </Text>
                        </Box>

                        {/* EMAIL */}
                        <FormControl isInvalid={!!formik.errors.email}>
                            <FormLabel>Email address</FormLabel>
                            <InputGroup>
                                <Input
                                    onChange={formik.handleChange}
                                    onBlur={formik.handleBlur}
                                    value={formik.values.email}
                                    name="email"
                                    type="email"
                                    autoComplete="email"
                                    placeholder="john@company.com"
                                />
                                <InputRightElement pointerEvents="none" opacity={0.7}>
                                    <Icon as={FiMail} />
                                </InputRightElement>
                            </InputGroup>
                            <FormErrorMessage>{formik.errors.email}</FormErrorMessage>
                        </FormControl>

                        {/* PASSWORD */}
                        <FormControl isInvalid={!!formik.errors.password}>
                            <FormLabel>Password</FormLabel>
                            <InputGroup>
                                <Input
                                    onChange={formik.handleChange}
                                    onBlur={formik.handleBlur}
                                    value={formik.values.password}
                                    name="password"
                                    type={showPassword ? 'text' : 'password'}
                                    autoComplete="current-password"
                                    placeholder="••••••••"
                                />
                                <InputRightElement>
                                    <Button
                                        size="sm"
                                        variant="ghost"
                                        onClick={() => setShowPassword((s) => !s)}
                                        aria-label={showPassword ? 'Hide password' : 'Show password'}
                                    >
                                        {showPassword ? <ViewOffIcon /> : <ViewIcon />}
                                    </Button>
                                </InputRightElement>
                            </InputGroup>
                            <FormErrorMessage>{formik.errors.password}</FormErrorMessage>
                        </FormControl>

                        <Button
                            type="submit"
                            colorScheme="pink"
                            isLoading={formik.isSubmitting}
                            isDisabled={formik.isSubmitting}
                            width="100%"
                            size="lg"
                        >
                            Submit
                        </Button>

                        <Divider />

                        <HStack w="100%" justify="center">
                            <Text fontSize="sm" color={subtleText}>
                                Don&apos;t have an account?
                            </Text>
                            <Button as={Link} to="/" variant="outline" size="sm">
                                Register
                            </Button>
                        </HStack>
                    </VStack>
                </Container>
            </Flex>
        </Stack>
    )
}



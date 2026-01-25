// src/pages/Register.jsx
import React, { useMemo, useState } from 'react'
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
  Radio,
  RadioGroup,
  Stack,
  Text,
  useBreakpointValue,
  useColorModeValue,
  useToast,
  VStack,
  Box,
} from '@chakra-ui/react'
import { ViewIcon, ViewOffIcon } from '@chakra-ui/icons'
import { FiUser, FiMail, FiLock } from 'react-icons/fi'
import { useFormik } from 'formik'
import { Link, useNavigate } from 'react-router-dom'
import AuthService from '../services/AuthService'
import svg from '../svgs/main.svg'

function Register() {
  const authService = useMemo(() => new AuthService(), [])
  const toast = useToast()
  const navigate = useNavigate()

  const [showPassword, setShowPassword] = useState(false)

  const cardBg = useColorModeValue('white', 'gray.800')
  const cardBorder = useColorModeValue('blackAlpha.200', 'whiteAlpha.200')
  const subtleText = useColorModeValue('gray.600', 'gray.300')

  const formik = useFormik({
    initialValues: {
      name: '',
      lastName: '',
      email: '',
      password: '',
      accountType: 'USER', // USER | ORGANIZER (request)
    },
    onSubmit: async (values, { setSubmitting, setErrors }) => {
      try {
        const payload = {
          name: values.name,
          lastName: values.lastName,
          email: values.email,
          password: values.password,
          organizerRequest: values.accountType === 'ORGANIZER',
        }

        await authService.register(payload)

        toast({
          title: 'Almost done ✅',
          description: payload.organizerRequest
              ? 'Account created. Please confirm your email first. Organizer request will be reviewed after activation.'
              : 'Account created. Please confirm your email to activate your account.',
          status: 'success',
          duration: 9000,
          isClosable: true,
        })

        navigate('/login')
      } catch (e) {
        const status = e?.response?.status
        const data = e?.response?.data

        if (
            status === 409 &&
            (data?.code === 'EMAIL_EXISTS' ||
                data?.code === 'ALREADY_EXISTS' ||
                data?.message === 'EMAIL_ALREADY_EXISTS' ||
                data === 'EMAIL_ALREADY_EXISTS')
        ) {
          const emailMsg = data?.fieldErrors?.email || 'Email already exists'
          setErrors({ email: emailMsg })

          toast({
            title: 'Email already exists',
            description:
                'This email is already registered. Please log in or use another email.',
            status: 'error',
            duration: 9000,
            isClosable: true,
          })
          return
        }

        const fieldErrors =
            data?.fieldErrors ||
            data?.validation ||
            e?.fieldErrors ||
            e?.validation ||
            null

        if (fieldErrors) setErrors(fieldErrors)

        const msg =
            data?.message ||
            (typeof data === 'string' ? data : '') ||
            e?.message ||
            'Request failed'

        toast({
          title: status === 400 ? 'Validation failed' : 'Registration error',
          description: msg,
          status: 'error',
          duration: 9000,
          isClosable: true,
        })

        console.error('Register error:', e)
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
                      bg: 'blue.500',
                      zIndex: -1,
                    }}
                >
                  Event Planer
                </Text>
                <br />
                <Text color="blue.500" as="span">
                  Spring && React
                </Text>
              </Heading>

              <Text color={subtleText} fontSize="md" maxW="md">
                Create an account to plan events, join activities, and manage your
                profile.
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
                <Heading size="lg">Register</Heading>
                <Text mt={2} color={subtleText} fontSize="sm">
                  Fill in your details to create your account.
                </Text>
              </Box>

              {/* NAME */}
              <FormControl isInvalid={!!formik.errors.name && formik.touched.name}>
                <FormLabel>Name</FormLabel>
                <InputGroup>
                  <Input
                      name="name"
                      type="text"
                      autoComplete="given-name"
                      value={formik.values.name}
                      onChange={formik.handleChange}
                      onBlur={formik.handleBlur}
                      placeholder="John"
                      required
                  />
                  <InputRightElement pointerEvents="none" opacity={0.7}>
                    <Icon as={FiUser} />
                  </InputRightElement>
                </InputGroup>
                <FormErrorMessage>{formik.errors.name}</FormErrorMessage>
              </FormControl>

              {/* LAST NAME */}
              <FormControl
                  isInvalid={!!formik.errors.lastName && formik.touched.lastName}
              >
                <FormLabel>Last Name</FormLabel>
                <InputGroup>
                  <Input
                      name="lastName"
                      type="text"
                      autoComplete="family-name"
                      value={formik.values.lastName}
                      onChange={formik.handleChange}
                      onBlur={formik.handleBlur}
                      placeholder="Doe"
                      required
                  />
                  <InputRightElement pointerEvents="none" opacity={0.7}>
                    <Icon as={FiUser} />
                  </InputRightElement>
                </InputGroup>
                <FormErrorMessage>{formik.errors.lastName}</FormErrorMessage>
              </FormControl>

              {/* EMAIL */}
              <FormControl isInvalid={!!formik.errors.email && formik.touched.email}>
                <FormLabel>Email address</FormLabel>
                <InputGroup>
                  <Input
                      name="email"
                      type="email"
                      autoComplete="email"
                      value={formik.values.email}
                      onChange={formik.handleChange}
                      onBlur={formik.handleBlur}
                      placeholder="john@company.com"
                      required
                  />
                  <InputRightElement pointerEvents="none" opacity={0.7}>
                    <Icon as={FiMail} />
                  </InputRightElement>
                </InputGroup>
                <FormErrorMessage>{formik.errors.email}</FormErrorMessage>
              </FormControl>

              {/* PASSWORD */}
              <FormControl
                  isInvalid={!!formik.errors.password && formik.touched.password}
              >
                <FormLabel>Password</FormLabel>
                <InputGroup>
                  <Input
                      name="password"
                      type={showPassword ? 'text' : 'password'}
                      autoComplete="new-password"
                      value={formik.values.password}
                      onChange={formik.handleChange}
                      onBlur={formik.handleBlur}
                      placeholder="••••••••"
                      required
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

              {/* ACCOUNT TYPE */}
              <FormControl>
                <FormLabel>Account type</FormLabel>
                <RadioGroup
                    name="accountType"
                    value={formik.values.accountType}
                    onChange={(val) => formik.setFieldValue('accountType', val)}
                >
                  <HStack spacing={6} flexWrap="wrap">
                    <Radio value="USER">USER</Radio>
                    <Radio value="ORGANIZER">ORGANIZER (request)</Radio>
                  </HStack>
                </RadioGroup>

                {formik.values.accountType === 'ORGANIZER' && (
                    <Text mt={2} fontSize="sm" color={subtleText}>
                      Organizer mode requires approval after you verify your email.
                    </Text>
                )}
              </FormControl>

              <Button
                  type="submit"
                  colorScheme="pink"
                  isLoading={formik.isSubmitting}
                  isDisabled={formik.isSubmitting}
                  width="100%"
                  size="lg"
              >
                Create account
              </Button>

              <Divider />

              <HStack w="100%" justify="center">
                <Text fontSize="sm" color={subtleText}>
                  Already have an account?
                </Text>
                <Button as={Link} to="/login" variant="outline" size="sm">
                  Login
                </Button>
              </HStack>
            </VStack>
          </Container>
        </Flex>
      </Stack>
  )
}

export default Register

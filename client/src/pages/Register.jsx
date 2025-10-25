import { Button, Container, Flex, FormControl, FormLabel, Heading, Image, Input,
  Stack, Text, useBreakpointValue, useToast, VStack, FormErrorMessage, RadioGroup, HStack, Radio } from '@chakra-ui/react'
import { useFormik } from 'formik'
import React, { useContext } from 'react'
import { Link } from 'react-router-dom'
import AuthContext from '../context/AuthContext'
import AuthService from '../services/AuthService'
import svg from '../svgs/main.svg'

function Register() {
  const authService = new AuthService();
  const toast = useToast()
  const { login } = useContext(AuthContext)

  const formik = useFormik({
    initialValues: { name: '', lastName: '', email: '', password: '', role: 'USER' },
    onSubmit: async (values, { setSubmitting, setErrors }) => {
      try {
        const { data } = await authService.register(values) // очікуємо токен у data
        login(data)
        toast({
          title: 'Register Successfully',
          description: "We've created your account for you.",
          status: 'success',
          duration: 9000,
          isClosable: true,
        })
      } catch (e) {
        // Підтримка двох форматів помилки: нормалізований і axios.raw
        const fieldErrors =
            e?.fieldErrors ||
            e?.validation ||
            e?.response?.data?.fieldErrors ||
            e?.response?.data?.validation ||
            null

        if (fieldErrors) setErrors(fieldErrors)

        const msg =
            e?.message ||
            e?.response?.data?.message ||
            (typeof e?.response?.data === 'string' ? e.response.data : '') ||
            'Request failed'

        toast({
          title: (e?.status || e?.response?.status) === 400 ? 'Validation failed' : 'Registration error',
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
      <Stack direction={'row'} spacing={0} minH={'100vh'}>
        <Flex alignItems={'center'} justifyContent={'center'} width={{ base: 0, md: '100%', lg: '100%' }}>
          <VStack p={10} spacing={5}>
            <Stack spacing={6} w={'full'} maxW={'lg'}>
              <Heading fontSize={{ base: '0', md: '5xl', lg: '6xl' }}>
                <Text
                    as={'span'}
                    position={'relative'}
                    _after={{
                      content: "''",
                      width: 'full',
                      height: useBreakpointValue({ base: '20%', md: '30%' }),
                      position: 'absolute',
                      bottom: 1,
                      left: 0,
                      bg: 'pink.500',
                      zIndex: -1,
                    }}>
                  Spring-React
                </Text>
                <br />{' '}
                <Text color={'pink.500'} as={'span'}>
                  Social Media App
                </Text>{' '}
              </Heading>
            </Stack>
            <Image src={svg} />
          </VStack>
        </Flex>

        <Flex justifyContent={'center'} alignItems={'center'} width={'100%'}>
          <Container>
            <VStack as={'form'} p={10} onSubmit={formik.handleSubmit} borderRadius={'xl'} boxShadow={'2xl'} spacing={5}>
              <Heading>Register</Heading>

              <FormControl isInvalid={!!formik.errors.name && formik.touched.name}>
                <FormLabel>Name</FormLabel>
                <Input
                    name="name"
                    type="text"
                    autoComplete="given-name"
                    value={formik.values.name}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                    required
                />
                <FormErrorMessage>{formik.errors.name}</FormErrorMessage>
              </FormControl>

              <FormControl isInvalid={!!formik.errors.lastName && formik.touched.lastName}>
                <FormLabel>Last Name</FormLabel>
                <Input
                    name="lastName"
                    type="text"
                    autoComplete="family-name"
                    value={formik.values.lastName}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                    required
                />
                <FormErrorMessage>{formik.errors.lastName}</FormErrorMessage>
              </FormControl>

              <FormControl isInvalid={!!formik.errors.email && formik.touched.email}>
                <FormLabel>Email address</FormLabel>
                <Input
                    name="email"
                    type="email"
                    autoComplete="email"
                    value={formik.values.email}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                    required
                />
                <FormErrorMessage>{formik.errors.email}</FormErrorMessage>
              </FormControl>

              <FormControl isInvalid={!!formik.errors.password && formik.touched.password}>
                <FormLabel>Password</FormLabel>
                <Input
                    name="password"
                    type="password"
                    autoComplete="new-password"
                    value={formik.values.password}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                    required
                />
                <FormErrorMessage>{formik.errors.password}</FormErrorMessage>
              </FormControl>
              <FormControl>
                <FormLabel>Я —</FormLabel>
                <RadioGroup
                    name="role"
                    value={formik.values.role}
                    onChange={(val)=>formik.setFieldValue('role', val)}
                >
                  <HStack spacing={6}>
                    <Radio value="USER">Користувач</Radio>
                    <Radio value="ORGANIZER">Органайзер</Radio>
                  </HStack>
                </RadioGroup>
              </FormControl>

              <Button type="submit" colorScheme={'pink'} isLoading={formik.isSubmitting} isDisabled={formik.isSubmitting}>
                Register
              </Button>
              <Button as={Link} to="/login">Login</Button>
            </VStack>
          </Container>
        </Flex>
      </Stack>
  )
}

export default Register

import {
    Box,
    useColorModeValue,
    Stack,
    IconButton,
    Spacer,
    Tooltip,
    useColorMode,
} from '@chakra-ui/react'
import { useContext, useEffect, useMemo, useState } from 'react'
import { BiHome } from 'react-icons/bi'
import { CgProfile } from 'react-icons/cg'
import { MdConfirmationNumber } from 'react-icons/md'
import { FiCheckCircle } from 'react-icons/fi'
import { MoonIcon, SunIcon } from '@chakra-ui/icons'
import AuthContext from '../context/AuthContext'
import AddPost from '../pages/AddPost'
import NavItem from './NavItem'
import EventCheckerService from '../services/EventCheckerService'

function Nav() {
    const { user } = useContext(AuthContext)
    const [isChecker, setIsChecker] = useState(false)
    const svc = useMemo(() => new EventCheckerService(), [])

    const { colorMode, toggleColorMode } = useColorMode()

    useEffect(() => {
        const token = localStorage.getItem('token')
        if (!token || !user?.id) return
        svc.mine(token)
            .then(res => setIsChecker((res.data || []).length > 0))
            .catch(() => setIsChecker(false))
    }, [svc, user?.id])

    return (
        <Box
            top={{ lg: 4 }}
            zIndex={1}
            w={{ sm: '100%', lg: '30vh' }}
            position={{ sm: 'sticky', lg: 'fixed' }}
            px={5}
        >
            <Stack
                bg={useColorModeValue('white', 'gray.800')}
                color={useColorModeValue('gray.600', 'white')}
                borderRadius="2xl"
                spacing="6"
                p="15px"
                pt={{ lg: '10vh' }}
                h={{ sm: '20', lg: '95vh' }}
                direction={{ sm: 'row', lg: 'column' }}
                boxShadow="2xl"
            >
                <NavItem description="Home" icon={<BiHome />} path="/home" />
                <NavItem description="Profile" icon={<CgProfile />} path={`/profile/${user?.id}`} />
                <NavItem description="My Tickets" icon={<MdConfirmationNumber />} path="/tickets" />

                {(user?.role === 'ORGANIZER' || isChecker) && (
                    <NavItem description="Verifier" icon={<FiCheckCircle />} path="/verify" />
                )}

                <AddPost />

                {/* push theme button to bottom / right */}
                <Spacer />

                {/* THEME TOGGLE */}
                <Tooltip label={`Switch to ${colorMode === 'light' ? 'dark' : 'light'} mode`}>
                    <IconButton
                        aria-label="Toggle color mode"
                        icon={colorMode === 'light' ? <MoonIcon /> : <SunIcon />}
                        onClick={toggleColorMode}
                        variant="ghost"
                        alignSelf={{ sm: 'center', lg: 'flex-start' }}
                    />
                </Tooltip>
            </Stack>
        </Box>
    )
}

export default Nav

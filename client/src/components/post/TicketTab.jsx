
import { Button, Box, Text, useToast, Stack } from '@chakra-ui/react'
import { useEffect, useMemo, useState } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'

import TicketService from '../../services/TicketService'
import PaymentService from '../../services/PaymentService'
import QrTicketCard from '../QrTicketCard'

function fmtDate(d) {
    try {
        return new Intl.DateTimeFormat('pl-PL', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
        }).format(d)
    } catch {
        return ''
    }
}

export default function TicketTab({ post, myTicket, onRegistered }) {
    const ticketService = useMemo(() => new TicketService(), [])
    const paymentService = useMemo(() => new PaymentService(), [])
    const toast = useToast()
    const token = localStorage.getItem('token')

    const [params] = useSearchParams()
    const navigate = useNavigate()

    const [loadingRegister, setLoadingRegister] = useState(false)
    const [loadingPay, setLoadingPay] = useState(false)

    const isPublished = post.status === 'PUBLISHED'
    const isPaidEvent = Boolean(post?.paid) && Number(post?.price) > 0

    const priceLabel = new Intl.NumberFormat('pl-PL', {
        style: 'currency',
        currency: post?.currency || 'PLN'
    }).format(Number(post?.price || 0))

    const now = new Date()
    const salesStartAt = post?.salesStartAt ? new Date(post.salesStartAt) : null
    const salesEndAt = post?.salesEndAt ? new Date(post.salesEndAt) : null

    const salesNotStarted = salesStartAt && now < salesStartAt
    const salesEnded = salesEndAt && now > salesEndAt
    const canPay = isPaidEvent && isPublished && !salesNotStarted && !salesEnded

    useEffect(() => {
        if (myTicket) return
        if (!token) return

        const paid = params.get('paid')
        const sessionId = params.get('session_id')
        if (!paid && !sessionId) return

        let alive = true
        let tries = 0
        const maxTries = 20

        const poll = async () => {
            tries++
            try {
                const t = await ticketService.getMy(post.id, token)
                if (!alive) return
                onRegistered(t.data)
                navigate(`/posts/${post.id}`, { replace: true })
            } catch {
                if (!alive) return
                if (tries < maxTries) setTimeout(poll, 1000)
            }
        }

        poll()
        return () => { alive = false }
    }, [params, myTicket, token, post.id, ticketService, onRegistered, navigate])

    const handleRegister = async () => {
        try {
            setLoadingRegister(true)
            const res = await ticketService.register(post.id, token)
            toast({ title: 'Registered successfully', status: 'success', duration: 3000, isClosable: true })
            onRegistered(res.data)
        } catch (e) {
            const msg = e?.response?.data?.message || e?.response?.data || e?.message || 'Registration failed'
            toast({ title: 'Registration failed', description: msg, status: 'error', duration: 4000, isClosable: true })
        } finally {
            setLoadingRegister(false)
        }
    }

    const handlePay = async () => {
        if (!token) {
            toast({ title: 'Najpierw zaloguj się', status: 'warning', duration: 3000, isClosable: true })
            return
        }

        if (salesNotStarted) {
            toast({
                title: 'Sprzedaż jeszcze się nie rozpoczęła',
                description: salesStartAt ? `Start: ${fmtDate(salesStartAt)}` : undefined,
                status: 'info',
                duration: 5000,
                isClosable: true
            })
            return
        }

        if (salesEnded) {
            toast({
                title: 'Sprzedaż została zakończona',
                status: 'info',
                duration: 5000,
                isClosable: true
            })
            return
        }

        try {
            setLoadingPay(true)
            const { data } = await paymentService.createCheckoutSession(post.id, token)
            if (!data?.url) throw new Error('No checkout URL returned from server')
            window.location.href = data.url
        } catch (e) {
            const raw = e?.response?.data?.message || e?.response?.data || e?.message
            const msg =
                raw === 'SALES_NOT_STARTED' ? 'Sprzedaż jeszcze się nie rozpoczęła.' :
                    raw === 'SALES_ENDED' ? 'Sprzedaż została zakończona.' :
                        raw === 'EVENT_NOT_PUBLISHED' ? 'Wydarzenie nie jest opublikowane.' :
                            raw === 'EVENT_CANCELLED' ? 'Wydarzenie zostało anulowane.' :
                                raw || 'Payment failed'

            toast({ title: 'Payment failed', description: msg, status: 'error', duration: 5000, isClosable: true })
        } finally {
            setLoadingPay(false)
        }
    }

    if (myTicket) {
        return (
            <Box borderWidth="1px" p={4} borderRadius="md">
                <QrTicketCard postId={post.id} ticketCode={myTicket.code} status={myTicket.status} />
            </Box>
        )
    }

    if (!isPublished) {
        return <Text color="gray.500">Registration is not available.</Text>
    }

    if (isPaidEvent) {
        return (
            <Stack spacing={3}>
                <Text fontWeight="600">Price: {priceLabel}</Text>
                <Button onClick={handlePay} isLoading={loadingPay} isDisabled={!canPay} colorScheme="purple">
                    Pay
                </Button>

                {salesNotStarted && (
                    <Text fontSize="sm" opacity={0.7}>
                        Ticket sales will begin: {salesStartAt ? fmtDate(salesStartAt) : ''}
                    </Text>
                )}
                {salesEnded && (
                    <Text fontSize="sm" opacity={0.7}>
                        Ticket sales have ended.
                    </Text>
                )}
                {!salesNotStarted && !salesEnded && (
                    <Text fontSize="sm" opacity={0.7}>
                        After successful payment, the ticket will be created automatically.
                    </Text>
                )}
            </Stack>
        )
    }

    return (
        <Button onClick={handleRegister} isLoading={loadingRegister} colorScheme="pink">
            Register
        </Button>
    )
}

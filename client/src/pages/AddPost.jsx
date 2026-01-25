import { useFormik } from 'formik'
import React, { useContext, useState } from 'react'
import {
    Modal, ModalOverlay, ModalContent, ModalHeader, Button, ModalFooter, ModalBody,
    ModalCloseButton, FormLabel, Input, FormControl, useDisclosure, Textarea,
    useToast, Switch, HStack, Select, SimpleGrid
} from '@chakra-ui/react'
import PostService from '../services/PostService'
import PostImageService from '../services/PostImageService'
import AuthContext from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'

function toLdt(v){ // "YYYY-MM-DDTHH:mm" -> "YYYY-MM-DDTHH:mm:00"
    if(!v) return null;
    return v.length===16 ? v + ':00' : v;
}

function AddPost() {
    const { user } = useContext(AuthContext)

    const [file, setFile] = useState(null)
    const { isOpen, onOpen, onClose } = useDisclosure()
    const toast = useToast()
    const token = localStorage.getItem("token")
    const navigate = useNavigate()

    // краще створювати сервіси один раз (але можна і так лишити)
    const postService = new PostService()
    const postImageService = new PostImageService()

    const formik = useFormik({
        initialValues: {
            title: '',
            description: '',
            location: '',
            startAt: '',
            endAt: '',
            capacity: '',
            paid: false,
            price: '',
            currency: 'PLN',
            salesStartAt: '',
            salesEndAt: ''
        },
        onSubmit: async (v) => {
            try {
                const payload = {
                    title: v.title || null,
                    description: v.description || '',
                    location: v.location || null,
                    startAt: toLdt(v.startAt),
                    endAt: toLdt(v.endAt),
                    capacity: v.capacity ? parseInt(v.capacity,10) : null,
                    paid: !!v.paid,
                    price: v.paid ? (v.price ? parseFloat(v.price) : 0) : 0,
                    currency: v.paid ? (v.currency || 'PLN') : 'PLN',
                    salesStartAt: v.salesStartAt ? toLdt(v.salesStartAt) : null,
                    salesEndAt: v.salesEndAt ? toLdt(v.salesEndAt) : null
                }

                const { data: postId } = await postService.add(payload, token)

                if (file) {
                    const fd = new FormData()
                    fd.append("postId", postId)
                    fd.append("image", file)
                    await postImageService.upload(fd, token)
                }

                toast({ title: "Event created", status: 'success', duration: 6000, isClosable: true })
                onClose()
                navigate(`/profile/${user?.id}`)
            } catch (error) {
                console.error(error)
                toast({ title: "Failed to create", status: 'error', duration: 6000, isClosable: true })
            }
        }
    })

    if (!user || user.role !== 'ORGANIZER') return null;

    return (
        <>
            <Button onClick={onOpen} colorScheme={'pink'}>Create Event</Button>
            <Modal isOpen={isOpen} onClose={onClose} size="lg">
                <ModalOverlay />
                <ModalContent as={'form'} onSubmit={formik.handleSubmit}>
                    <ModalHeader>Create event</ModalHeader>
                    <ModalCloseButton />
                    <ModalBody pb={6}>
                        <FormControl mb={3}>
                            <FormLabel>Title</FormLabel>
                            <Input name="title" value={formik.values.title} onChange={formik.handleChange}/>
                        </FormControl>

                        <FormControl mb={3}>
                            <FormLabel>Description</FormLabel>
                            <Textarea name="description" value={formik.values.description} onChange={formik.handleChange}/>
                        </FormControl>

                        <FormControl mb={3}>
                            <FormLabel>Location</FormLabel>
                            <Input name="location" value={formik.values.location} onChange={formik.handleChange}/>
                        </FormControl>

                        <SimpleGrid columns={2} spacing={3}>
                            <FormControl>
                                <FormLabel>Start</FormLabel>
                                <Input type="datetime-local" name="startAt" value={formik.values.startAt} onChange={formik.handleChange}/>
                            </FormControl>
                            <FormControl>
                                <FormLabel>End</FormLabel>
                                <Input type="datetime-local" name="endAt" value={formik.values.endAt} onChange={formik.handleChange}/>
                            </FormControl>
                        </SimpleGrid>

                        <SimpleGrid columns={2} spacing={3} mt={3}>
                            <FormControl>
                                <FormLabel>Capacity (optional)</FormLabel>
                                <Input type="number" min={0} name="capacity" value={formik.values.capacity} onChange={formik.handleChange}/>
                            </FormControl>

                            <FormControl>
                                <FormLabel>Paid</FormLabel>
                                <HStack>
                                    <Switch
                                        isChecked={formik.values.paid}
                                        onChange={(e)=>formik.setFieldValue('paid', e.target.checked)}
                                    />
                                    <span>{formik.values.paid ? 'Paid' : 'Free'}</span>
                                </HStack>
                            </FormControl>
                        </SimpleGrid>

                        {formik.values.paid && (
                            <SimpleGrid columns={2} spacing={3} mt={3}>
                                <FormControl>
                                    <FormLabel>Price</FormLabel>
                                    <Input type="number" step="0.01" min={0} name="price" value={formik.values.price} onChange={formik.handleChange}/>
                                </FormControl>
                                <FormControl>
                                    <FormLabel>Currency</FormLabel>
                                    <Select name="currency" value={formik.values.currency} onChange={formik.handleChange}>
                                        <option value="PLN">PLN</option>
                                        <option value="EUR">EUR</option>
                                        <option value="USD">USD</option>
                                    </Select>
                                </FormControl>
                            </SimpleGrid>
                        )}

                        <SimpleGrid columns={2} spacing={3} mt={3}>
                            <FormControl>
                                <FormLabel>Sales start (optional)</FormLabel>
                                <Input type="datetime-local" name="salesStartAt" value={formik.values.salesStartAt} onChange={formik.handleChange}/>
                            </FormControl>
                            <FormControl>
                                <FormLabel>Sales end (optional)</FormLabel>
                                <Input type="datetime-local" name="salesEndAt" value={formik.values.salesEndAt} onChange={formik.handleChange}/>
                            </FormControl>
                        </SimpleGrid>

                        <FormControl mt={4}>
                            <FormLabel>Cover image</FormLabel>
                            <Button as="label" variant="outline">
                                {file ? file.name : 'Upload image'}
                                <input hidden type="file" accept="image/*" onChange={(e)=>setFile(e.target.files?.[0] || null)} />
                            </Button>
                        </FormControl>
                    </ModalBody>

                    <ModalFooter>
                        <Button type='submit' colorScheme='pink' mr={3}>Save</Button>
                        <Button onClick={onClose}>Cancel</Button>
                    </ModalFooter>
                </ModalContent>
            </Modal>
        </>
    )
}

export default AddPost

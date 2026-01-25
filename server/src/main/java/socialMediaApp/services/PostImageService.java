package socialMediaApp.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import socialMediaApp.api.exp.NotFoundException;
import socialMediaApp.mappers.PostImageMapper;
import socialMediaApp.models.PostImage;
import socialMediaApp.repositories.PostImageRepository;
import socialMediaApp.responses.postImage.PostImageResponse;
import socialMediaApp.utils.ImageUtil;

import java.io.IOException;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostImageService {


    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024; // 10MB

    private final PostImageRepository postImageRepository;
    private final PostService postService;
    private final PostImageMapper postImageMapper;

    public PostImageService(PostImageRepository postImageRepository,
                            PostService postService,
                            PostImageMapper postImageMapper) {
        this.postImageRepository = postImageRepository;
        this.postService = postService;
        this.postImageMapper = postImageMapper;
    }

    @Transactional
    public PostImageResponse upload(MultipartFile file, int postId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new IllegalArgumentException("Only image/* content types are allowed");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("Image is too large. Max 10MB");
        }


        var post = postService.getById(postId);


        Optional<PostImage> existing = postImageRepository.findPostImageByPost_Id(postId);

        PostImage img = existing.orElseGet(PostImage::new);
        img.setPost(post);
        img.setName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "image");
        img.setType(contentType);
        img.setData(ImageUtil.compressImage(file.getBytes()));

        postImageRepository.save(img);

        return postImageMapper.imageToResponse(img);
    }

    public byte[] download(int postId) {
        PostImage postImage = postImageRepository.findPostImageByPost_Id(postId)
                .orElseThrow(() -> new NotFoundException("Post image not found: postId=" + postId));

        try {
            return ImageUtil.decompressImage(postImage.getData());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Corrupted image data");
        }
    }
}

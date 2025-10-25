package com.example.backend.controller;

import com.example.backend.model.UserPrincipal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.backend.repository.PostRepo;
import com.example.backend.repository.UserRepo;
import com.example.backend.model.Post;
import jakarta.servlet.http.HttpServletRequest;

@RestController()
@EnableMethodSecurity
@RequestMapping("/api")
public class PostController {

    PostResponseDTO convertToResponseDTO(Post post) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.id = post.getId();
        dto.title = post.getTitle();
        dto.body = post.getBody();
        dto.parentId = post.getParentId();
        dto.answered = post.isAnswered();
        dto.subject = post.getSubject();
        dto.createdAt = post.getCreatedAt();
        dto.username = post.getUser() != null ? post.getUser().getUsername() : null;
        dto.userPfp = "https://ui-avatars.com/api/?name=" + (post.getUser() != null ? post.getUser().getUsername() : "Unknown") + "&background=random&size=128";
        return dto;
    }
    

    @Autowired
    private PostRepo postRepo;

    @Autowired
    private UserRepo userRepo;

    @GetMapping("/user/tasks")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public List<Post> getTasks(HttpServletRequest request) throws Exception {
        Authentication authentication = (Authentication) SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (userPrincipal != null) {
            return postRepo.findByUserId(userPrincipal.getId());
        }
        throw new Exception("User not authenticated");
    }

    @GetMapping("/user/posts")
    public Page<PostResponseDTO> getPosts(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepo.findAllByParentId(null,pageable);
        return  posts.map(z -> convertToResponseDTO(z));
    }

     @GetMapping("/user/posts/replies/{parentId}")
    public Page<PostResponseDTO> getReplies(
    @PathVariable Integer parentId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepo.findAllByParentId(parentId,pageable);
        return  posts.map(z -> convertToResponseDTO(z));
    }
    
    
    

    @GetMapping("/user/posts/{id}")
    public PostResponseDTO getPost(HttpServletRequest request,
        @PathVariable Integer id)
        
        throws Exception {
        Optional<Post> postOpt = postRepo.findById(id);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            return convertToResponseDTO(post);
        } else {
            throw new Exception("Post not found");
        }

    }

    @DeleteMapping("/admin/tasks/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void removeTask(@PathVariable Integer id) throws Exception {
        Optional<Post> optional = postRepo.findById(id);
        if (optional.isPresent()) {
            postRepo.deleteById(id);
        } else {
            throw new Exception("Task not found");
        }
    }
    @PutMapping("/admin/tasks/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Post updateTask(@PathVariable Integer id,@RequestBody Post task) throws Exception {
        Optional<Post> optional = postRepo.findById(id);
        if (optional.isPresent()) 
        {
            Post existingTask = optional.get();
            existingTask.setUser(task.getUser());
            existingTask.setTitle(task.getTitle());
            existingTask.setBody(task.getBody());
            existingTask.setAnswered(task.isAnswered() || existingTask.isAnswered());
            return postRepo.save(existingTask);
        } else {
            throw new Exception("Task not found");
        }
    }

    @PostMapping("/user/posts")
    //@PreAuthorize("hasRole('ADMIN')")
    public PostResponseDTO createPost(@RequestBody PostRequestDTO  postDTO) {
        Authentication authentication = (Authentication) SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Post post = new Post();
        post.setUser( userRepo.findByUsername(userPrincipal.getUsername()));
        post.setTitle(postDTO.title);
        post.setBody(postDTO.body);
        post.setParentId(postDTO.parentId);
        post.setAnswered(false);
        post.setSubject(postDTO.subject);
        post.setCreatedAt(postDTO.createdAt);
        postRepo.save(post);
        //post.setCreatedAt(new Date(System.currentTimeMillis()));
        return convertToResponseDTO(post);
    }

    @PutMapping("/user/tasks/{id}")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public void completeTask(@PathVariable Integer id) throws Exception {
        Optional<Post> optional = postRepo.findById(id);
        if (optional.isPresent()) 
        {
            Post existingTask = optional.get();
            existingTask.setAnswered(!existingTask.isAnswered());
            postRepo.save(existingTask);
        } else {
            throw new Exception("Task not found");
        }
    }

}

class PostRequestDTO {
    public int id;
    public String title;
    public String body;
    public Integer parentId;
    public String subject;
    public LocalDateTime createdAt;
}

class PostResponseDTO {
    public int id;
    public String title;
    public String body;
    public Integer parentId;
    public String subject;

    public boolean answered;
    public LocalDateTime createdAt;

    public String username;
    public String userPfp;

    public List<PostResponseDTO> replies;
}
package com.example.first.controller;

import com.example.first.dto.BoardDto;
import com.example.first.dto.request.BoardRequestDto;
import com.example.first.entity.BoardEntity;
import com.example.first.entity.User;
import com.example.first.repository.UserRepository;
import com.example.first.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final UserRepository userRepository;
    // 게시글 등록
    @PostMapping("/players/{playerId}/boards")
    public ResponseEntity<String> create(
            Authentication auth,
            @PathVariable Long playerId,
            @Valid @RequestBody BoardRequestDto request
    ){
        if (tryAuthAndSetName(auth, request)) return new ResponseEntity<>("인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED);
        request.setPlayerId(playerId);
     BoardEntity saved = boardService.create(request);
     if(toDto(saved) != null) {
         return new ResponseEntity<>("입력되었습니다.", HttpStatus.CREATED);
     }
     return new ResponseEntity<>("정보 처리 중 오류가 발생했습니다.", HttpStatus.BAD_REQUEST);
    }

    // 특정 선수 응원글 조회 추가
    @GetMapping("/players/{playerId}/boards")
    public Page<BoardDto> listByPlayer(
            @PathVariable Long playerId,
            Pageable pageable
    ) {
        // Pageable → 기존 service 방식으로 변환
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        Sort sort = pageable.getSort();
        // 정렬이 여러개일 가능성도 있으므로 첫 번째만 사용
        Sort.Order order = sort.isEmpty() ? Sort.Order.desc("createdAt") : sort.iterator().next();
        String sortBy = order.getProperty();
        String direction = order.getDirection().isAscending() ? "asc" : "desc";
        return boardService.findByPlayerIdPaged(playerId, page, size, sortBy, direction)
                .map(this::toDto);
    }

    // 게시글 수정을 위한 조회 엔드포인트
    @GetMapping("/{id}")
    public ResponseEntity<?> detail(Authentication auth,
                           @PathVariable Long id
    ) {
        if(notYourBusiness(auth, id, false)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        //admin 전용 옵션도 추가할 필요 있음
        return new ResponseEntity<>(toDto(boardService.findById(id)), HttpStatus.OK);
    }

    @GetMapping("/my")
    public ResponseEntity<List<BoardDto>> getMy(Authentication auth) {
        if(auth == null || !auth.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<User> user = userRepository.findByUsername(auth.getName());
        if(user.isPresent()) {
            List<BoardDto> boarder = boardService.findMy(auth.getName());
            return new ResponseEntity<>(boarder, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> update(Authentication auth,
                                         @PathVariable Long id,
                                         @RequestBody BoardRequestDto request) {
        if (notYourBusiness(auth, id, false)) return new ResponseEntity<>("인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED);
        BoardEntity updated = new BoardEntity(
                request.getTitle(),
                request.getContent(),
                request.getAuthor(),
                null
        );
        boardService.update(id, updated);
        return new ResponseEntity<>("수정되었습니다.", HttpStatus.NO_CONTENT);
        //return toDto(boardService.update(id, updated));
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(Authentication auth, @PathVariable Long id) {
        //Below is dangerous since it does not compare User, but the String Author. May there be better ways...
        if (notYourBusiness(auth, id, true)) {
            return new ResponseEntity<>("올바르지 않은 요청입니다.", HttpStatus.BAD_REQUEST);
        } //bad request for all, it's on my purpose
        boardService.delete(id);
        return new ResponseEntity<>("삭제되었습니다.", HttpStatus.NO_CONTENT);
    }

    // Entity -> Dto 변환
    private BoardDto toDto(BoardEntity board) {
        return BoardDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .author(board.getAuthor())
                .views(board.getViews())
                .media(board.getMedia())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .playerId(board.getPlayer().getId())
                .build();
    }

    // 최신순
    @GetMapping("/latest")
    public List<BoardDto> listLatest() {
        return boardService.listByLatest().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // 조회수순
    @GetMapping("/popular")
    public List<BoardDto> listByViews() {
        return boardService.listByViews().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // 페이징 목록: /boards/page?page=0&size=10&sortBy=createdAt&dir=desc
    @GetMapping("/page")
    public Page<BoardDto> pagedList(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10")int size,
                                    @RequestParam(defaultValue = "createdAt") String sortBy,
                                    @RequestParam(defaultValue = "desc") String dir) {
        Page<BoardEntity> entityPage = boardService.findPaged(page, size, sortBy, dir);
        List<BoardDto> dtoList = entityPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, entityPage.getPageable(), entityPage.getTotalElements());
    }

    // 검색 + 페이징: /boards/search?keyword=민지&page=0&size=10
    @GetMapping("/search")
    public Page<BoardDto> search(@RequestParam String keyword,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10")int size,
                                 @RequestParam(defaultValue = "createdAt") String sortBy,
                                 @RequestParam(defaultValue = "desc") String dir) {
        Page<BoardEntity> entityPage = boardService.search(keyword, page, size, sortBy, dir);
        List<BoardDto> dtoList = entityPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, entityPage.getPageable(), entityPage.getTotalElements());
    }
    private boolean tryAuthAndSetName(Authentication auth, @RequestBody BoardRequestDto request) {
        if (auth == null || !auth.isAuthenticated()) {
            return true;
        }
        String currentUsername = auth.getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
        String displayAuthor = user.getEmail();
        if (displayAuthor == null || displayAuthor.isEmpty()) {
            displayAuthor = user.getUsername();
        }
        request.setAuthor(displayAuthor);
        return false;
    }
    private boolean notYourBusiness(Authentication auth, Long id, boolean allowAdmin) {
        if(auth == null || !auth.isAuthenticated()) {
            System.out.println("Not authenticated");
            return true;
        }
        Optional<User> user = userRepository.findByUsername(auth.getName());
        if(user.isEmpty()) {
            System.out.println("No such user");
            return true;
        }
        BoardEntity board = boardService.findById(id);
        if(!user.get().getUsername().equals(board.getAuthor())) {
            if(!user.get().isAdmin()) {
                System.out.println("Not your business");
            }
            else if(!allowAdmin) {
                System.out.println("Even if you are an admin, it's not your business");
                return true;
            }
            return false;
        }
        return false;
        //return !(user.get().getUsername().equals(board.getAuthor()) || user.get().isAdmin());
    }
}

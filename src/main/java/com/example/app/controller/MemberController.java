package com.example.app.controller;

import com.example.app.dto.MemberRequest;
import com.example.app.entity.Member;
import com.example.app.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@Tag(name = "Members", description = "Gym member management")
public class MemberController {

  @Autowired private MemberService memberService;

  @GetMapping
  @Operation(summary = "List members (paginated), optionally search by name/email/phone")
  public ResponseEntity<Page<Member>> list(
      @RequestParam(required = false) String search,
      @PageableDefault(size = 20) Pageable pageable) {
    if (search != null && !search.isBlank()) {
      return ResponseEntity.ok(memberService.search(search, pageable));
    }
    return ResponseEntity.ok(memberService.list(pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a member by id")
  public ResponseEntity<Member> get(@PathVariable Long id) {
    return ResponseEntity.ok(memberService.get(id));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Add a member (admin only)")
  public ResponseEntity<Member> create(@Valid @RequestBody MemberRequest request) {
    return ResponseEntity.ok(memberService.create(request));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update a member (admin only)")
  public ResponseEntity<Member> update(
      @PathVariable Long id, @Valid @RequestBody MemberRequest request) {
    return ResponseEntity.ok(memberService.update(id, request));
  }

  @PutMapping("/{id}/trainer/{trainerId}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Assign a trainer to a member (admin only)")
  public ResponseEntity<Member> assignTrainer(@PathVariable Long id, @PathVariable Long trainerId) {
    return ResponseEntity.ok(memberService.assignTrainer(id, trainerId));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete/deactivate a member (admin only)")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    memberService.delete(id);
    return ResponseEntity.noContent().build();
  }
}

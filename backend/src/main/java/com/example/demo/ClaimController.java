package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/claims")
public class ClaimController {

    private final ClaimRepository repository;

    public ClaimController(ClaimRepository repository) {
        this.repository = repository;
    }

    /**
     * Submit a new claim
     * POST /api/v1/claims
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Claim submitClaim(@RequestBody Claim claim) {
        if (claim.getStatus() == null) {
            claim.setStatus("SUBMITTED");
        }
        return repository.save(claim);
    }

    /**
     * Get all claims
     * GET /api/v1/claims
     */
    @GetMapping
    public List<Claim> getAllClaims() {
        return repository.findAll();
    }

    /**
     * Get a specific claim by ID
     * GET /api/v1/claims/{id}
     */
    @GetMapping("/{id}")
    public Optional<Claim> getClaimById(@PathVariable Long id) {
        return repository.findById(id);
    }

    /**
     * Update a claim's status
     * PUT /api/v1/claims/{id}
     */
    @PutMapping("/{id}")
    public Claim updateClaim(@PathVariable Long id, @RequestBody Claim updatedClaim) {
        return repository.findById(id)
                .map(claim -> {
                    if (updatedClaim.getStatus() != null) {
                        claim.setStatus(updatedClaim.getStatus());
                    }
                    if (updatedClaim.getClaimAmount() != null) {
                        claim.setClaimAmount(updatedClaim.getClaimAmount());
                    }
                    return repository.save(claim);
                })
                .orElseThrow(() -> new RuntimeException("Claim not found with id: " + id));
    }

    /**
     * Delete a claim
     * DELETE /api/v1/claims/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClaim(@PathVariable Long id) {
        repository.deleteById(id);
    }
}

package com.fidelity.mts.controller;

import com.fidelity.mts.domain.exception.AccountNotFoundException;
import com.fidelity.mts.service.RewardServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/accounts/rewards")
public class RewardController {

    @Autowired
    private RewardServiceInterface rewardService;

    @PostMapping("/{accountId}/redeem")
    public ResponseEntity<?> redeemRewards(@PathVariable long accountId) {
        try {
            int redeemedPoints = rewardService.redeemPoints(accountId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Successfully redeemed " + redeemedPoints + " points.");
            response.put("pointsRedeemed", redeemedPoints);
            response.put("amountAdded", redeemedPoints);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

        } catch (AccountNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Account not found.");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An unexpected error occurred: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
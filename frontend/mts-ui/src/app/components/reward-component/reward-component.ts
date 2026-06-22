import { CommonModule, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../service/auth-service';
import { RewardService } from '../../service/reward-service';
import { Reward } from '../../models/reward';

@Component({
  selector: 'app-reward',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './reward-component.html',
  styleUrl: './reward-component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RewardComponent implements OnInit {
  private readonly rewardService = inject(RewardService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly rewards = signal<Reward[]>([]);
  readonly isLoading = signal(true);
  readonly isRedeeming = signal(false); // New signal for button loading state
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null); // New signal for success feedback
  readonly totalPoints = signal(0);

  ngOnInit(): void {
    const accountId = this.authService.getAccountId();

    if (!accountId) {
      this.errorMessage.set('No account found. Please sign in again.');
      this.isLoading.set(false);
      return;
    }

    this.loadRewards(accountId);
  }

  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  // New method to handle redemption
  redeemRewards(): void {
    const accountId = this.authService.getAccountId();
    const points = this.totalPoints();

    if (!accountId || points <= 0) return;

    this.isRedeeming.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    // Assuming you add a redeemPoints method to your RewardService that takes accountId and points
    this.rewardService.redeemPoints(accountId, points).subscribe({
      next: () => {
        this.successMessage.set(`Successfully redeemed ${points} points for $${points}!`);
        this.isRedeeming.set(false);

        // Reload rewards to reflect the updated state
        // (Assuming your backend marks them as redeemed or deletes them)
        this.loadRewards(accountId);
      },
      error: (error: Error) => {
        this.errorMessage.set(error.message || 'Unable to redeem rewards at this time.');
        this.isRedeeming.set(false);
      },
    });
  }

  private loadRewards(accountId: number): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.rewardService.getRewards(accountId).subscribe({
      next: (rewards) => {
        this.rewards.set(rewards);
        this.totalPoints.set(rewards.reduce((sum, reward) => sum + reward.pointsAwarded, 0));
        this.isLoading.set(false);
      },
      error: (error: Error) => {
        this.errorMessage.set(error.message || 'Unable to load rewards.');
        this.isLoading.set(false);
      },
    });
  }
}

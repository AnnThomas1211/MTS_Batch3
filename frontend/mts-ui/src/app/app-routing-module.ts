import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard-component/dashboard-component';
import { TransferComponent } from './components/transfer-component/transfer-component';
import { HistoryComponent } from './components/history-component/history-component';
import { ProfileComponent } from './components/profile-component/profile-component';
import { LoginComponent } from './components/login-component/login-component';
import { StartPageComponent } from './components/start-page-component/start-page-component';
import { AuthGuard } from './guards/auth-guard';

const routes: Routes = [
  // ── Public routes ────────────────────────────────────────
  {
    path: 'start',
    component: StartPageComponent,
  },
  {
    path: 'admin-login',
    component: LoginComponent,
  },
  // Backwards-compat redirect: /login → /start
  {
    path: 'login',
    redirectTo: 'start',
    pathMatch: 'full',
  },

  // ── Protected routes ─────────────────────────────────────
  {
    path: 'transfer',
    component: TransferComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'history',
    component: HistoryComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'profile',
    component: ProfileComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'rewards',
    loadComponent: () =>
      import('./components/reward-component/reward-component').then((m) => m.RewardComponent),
    canActivate: [AuthGuard],
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard],
  },

  // ── Default routes ────────────────────────────────────────
  {
    path: '',
    redirectTo: 'start',
    pathMatch: 'full',
  },
  {
    path: '**',
    redirectTo: 'start',
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}

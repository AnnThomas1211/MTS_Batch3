import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard-component/dashboard-component';
import { TransferComponent } from './components/transfer-component/transfer-component';
import { HistoryComponent } from './components/history-component/history-component';
import { ProfileComponent } from './components/profile-component/profile-component';
import { LoginComponent } from './components/login-component/login-component';

const routes: Routes = [
  {
    path : 'transfer',
    component : TransferComponent
  },
  {
    path : 'history',
    component : HistoryComponent
  },
  {
    path : 'profile',
    component : ProfileComponent
  },
  {
    path : '',
    component : DashboardComponent
  },
  {
    path : 'dashboard',
  component : DashboardComponent
  },
  {
    path : 'login',
    component : LoginComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

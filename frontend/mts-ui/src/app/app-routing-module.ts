import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard-component/dashboard-component';
import { TransferComponent } from './components/transfer-component/transfer-component';
import { HistoryComponent } from './components/history-component/history-component';

const routes: Routes = [
  {
    path : '',
    component : DashboardComponent
  },
  {
    path : 'transfer',
    component : TransferComponent
  },
  {
    path : 'history',
    component : HistoryComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

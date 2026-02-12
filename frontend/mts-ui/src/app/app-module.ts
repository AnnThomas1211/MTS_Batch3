import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule, provideClientHydration, withEventReplay } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { LoginComponent } from './components/login-component/login-component';
import { DashboardComponent } from './components/dashboard-component/dashboard-component';
import { HistoryComponent } from './components/history-component/history-component';
import { ReactiveFormsModule } from '@angular/forms';


@NgModule({
  declarations: [
    App,
    LoginComponent,
    DashboardComponent,
    HistoryComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideClientHydration(withEventReplay()),
  ],
  bootstrap: [App]
})
export class AppModule { }

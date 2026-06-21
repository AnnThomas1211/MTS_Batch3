import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule, provideClientHydration, withEventReplay } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { LoginComponent } from './components/login-component/login-component';
import { DashboardComponent } from './components/dashboard-component/dashboard-component';
import { HistoryComponent } from './components/history-component/history-component';
import { ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, provideHttpClient, withFetch, withInterceptorsFromDi } from '@angular/common/http';
import { ProfileComponent } from './components/profile-component/profile-component';
import { AuthInterceptor } from './interceptors/auth-interceptor';
import { CommonModule } from '@angular/common';
import { TopNavbarComponent } from './components/top-navbar-component/top-navbar-component';
import { StartPageComponent } from './components/start-page-component/start-page-component';
import { RouterModule } from '@angular/router';


@NgModule({
  declarations: [
    App,
    TopNavbarComponent,
    LoginComponent,
    StartPageComponent,
    DashboardComponent,
    HistoryComponent,
    ProfileComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    CommonModule,
    RouterModule,
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideClientHydration(withEventReplay()),
    provideHttpClient(withFetch(), withInterceptorsFromDi()),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    },
  ],
  bootstrap: [App],
})
export class AppModule {}

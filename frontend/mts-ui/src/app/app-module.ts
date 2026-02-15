import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule, provideClientHydration, withEventReplay } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { LoginComponent } from './components/login-component/login-component';
import { DashboardComponent } from './components/dashboard-component/dashboard-component';
import { HistoryComponent } from './components/history-component/history-component';
import { ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, HttpClient, HttpClientModule, provideHttpClient, withFetch } from '@angular/common/http';
import { ProfileComponent } from './components/profile-component/profile-component';
import { AuthInterceptor } from './service/auth-interceptor';


@NgModule({
  declarations: [
    App,
    LoginComponent,
    DashboardComponent,
    HistoryComponent,
    ProfileComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule,
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    },
    provideBrowserGlobalErrorListeners(),
    provideClientHydration(withEventReplay()),
    provideHttpClient(withFetch()),
  ],
  bootstrap: [App],
})
export class AppModule {}

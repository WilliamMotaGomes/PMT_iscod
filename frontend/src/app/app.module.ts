import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { RouterModule, Routes } from '@angular/router';

import { AppComponent } from './app.component';
import { LoginComponent } from './components/login.component';
import { DashboardComponent } from './components/dashboard.component';
import { TaskDetailComponent } from './components/task-detail.component';
import { RegisterComponent } from './components/register.component';
import { ApiService } from './services/api.service';
import { SessionService } from './services/session.service';
import { AuthHeaderInterceptor } from './services/auth-header.interceptor';

const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'task/:id', component: TaskDetailComponent },
];

@NgModule({
  declarations: [AppComponent, LoginComponent, DashboardComponent, TaskDetailComponent, RegisterComponent],
  imports: [BrowserModule, FormsModule, HttpClientModule, RouterModule.forRoot(routes)],
  providers: [
    ApiService,
    SessionService,
    { provide: HTTP_INTERCEPTORS, useClass: AuthHeaderInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

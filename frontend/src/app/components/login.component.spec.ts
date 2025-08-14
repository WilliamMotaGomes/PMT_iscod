import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { LoginComponent } from './login.component';
import { ApiService } from '../services/api.service';
import { SessionService } from '../services/session.service';

class ApiMock
{
    login()
    {
        return Promise.resolve({id: 42});
    }
}

class RouterMock
{
    navigate = jasmine.createSpy('navigate');
}

describe('LoginComponent', () =>
{
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let session: SessionService;
  let router: RouterMock;

  beforeEach(async () =>
  {
    router = new RouterMock();
    await TestBed.configureTestingModule({
      imports: [FormsModule],
      declarations: [LoginComponent],
      providers: [
        { provide: ApiService, useClass: ApiMock },
        SessionService,
        { provide: Router, useValue: router }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    session = TestBed.inject(SessionService);
  });

  it('should login and navigate to dashboard', async () =>
  {
    await component.login();
    expect(session.userId).toBe(42);
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });
});

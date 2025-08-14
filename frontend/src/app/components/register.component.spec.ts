import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RegisterComponent } from './register.component';
import { ApiService } from '../services/api.service';

class ApiMock
{
    register()
    {
        return Promise.resolve({id: 1});
    }
}
class RouterMock
{
    navigate = jasmine.createSpy('navigate');
}

describe('RegisterComponent', () =>
{
  let comp: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let router: RouterMock;

  beforeEach(async () =>
  {
    router = new RouterMock();
    await TestBed.configureTestingModule({
      imports: [FormsModule],
      declarations: [RegisterComponent],
      providers: [
        { provide: ApiService, useClass: ApiMock },
        { provide: Router, useValue: router }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    comp = fixture.componentInstance;
    comp.username = 'u';
    comp.email = 'u@example.com';
    comp.password = 'pwd';
  });

  it('should call register and navigate to login', async () => {
    await comp.register();
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });
});

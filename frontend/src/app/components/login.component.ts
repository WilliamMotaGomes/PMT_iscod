import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../services/api.service';
import { SessionService } from '../services/session.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html'
})
export class LoginComponent
{
  email = 'william@example.com';
  password = 'password';
  loading = false;

  constructor(private api: ApiService, private session: SessionService, private router: Router) {}

  /* Connecte un utilisateur */
  async login()
  {
    try
    {
      this.loading = true;
      const user = await this.api.login(this.email, this.password);
      this.session.setUserId(user.id);
      this.router.navigate(['/dashboard']);
    }
    catch
    {
      alert('Échec de connexion');
    }
    finally
    {
      this.loading = false;
    }
  }
}

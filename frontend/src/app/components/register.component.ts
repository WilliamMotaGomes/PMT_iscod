import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../services/api.service';

@Component({
  selector: 'app-register',
  templateUrl: `register.component.html`
})
export class RegisterComponent
{
  username = '';
  email = '';
  password = '';
  loading = false;
  error: string | null = null;

  constructor(private api: ApiService, private router: Router){}

  /* Enrengistre un utilisateur */
  async register()
  {
    this.error = null;
    this.loading = true;
    try
    {
      await this.api.register(this.username, this.email, this.password);
      alert('Compte créé ! Vous pouvez vous connecter.');
      this.router.navigate(['/']);
    }
    catch (e:any)
    {
      this.error = 'Échec de l’inscription.';
    }
    finally
    {
      this.loading = false;
    }
  }
}

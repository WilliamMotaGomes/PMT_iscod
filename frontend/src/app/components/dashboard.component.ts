import { Component, OnInit } from '@angular/core';
import { ApiService, CreateTaskPayload } from '../services/api.service';
import { SessionService } from '../services/session.service';
import { Router } from '@angular/router';

type Role = 'ADMIN'|'MEMBER'|'OBSERVER';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html'
})

export class DashboardComponent implements OnInit
{
  projects: any[] = [];
  projectId: number | null = null;
  role: Role | null = null;
  inviteEmail = '';
  membersList: any[] = [];
  pname=''; pdesc=''; pstart='';
  tname=''; tdesc=''; tdue=''; tprio:'LOW'|'MEDIUM'|'HIGH'='MEDIUM';
  filterStatus = '';
  tasks: any[] = [];

  constructor(private api: ApiService, private session: SessionService, private router: Router){}

  get canAdmin()
  {
      return this.role === 'ADMIN';
  }

  get canEdit()
  {
      return this.role === 'ADMIN' || this.role === 'MEMBER';
      }

  async ngOnInit()
  {
    await this.loadProjects();
    const saved = localStorage.getItem('currentProjectId');
    if (saved) {
      this.onSelectProject(Number(saved));
    } else if (this.projects.length > 0) {
      this.onSelectProject(this.projects[0].id);
    }
  }

  /* Recupère les projets */
  async loadProjects()
  {
      this.projects = await this.api.projects();
  }

  /* Recupère le role pour un projet */
  async loadRole()
  {
      if(!this.projectId)
        return;

      const me = await this.api.me(this.projectId);
      this.role = (me && me.role) ? me.role : null;
  }

  /* Recupère les membres d'un projet */
  async loadMembers()
  {
      if(!this.projectId)
        return;

      this.membersList = await this.api.members(this.projectId);
  }

  /* Recupère les tâches d'un projet */
  async loadTasks()
  {
    if(!this.projectId)
        return;

    if(!this.role)
        await this.loadRole();

    const userId = this.session.userId!;
    this.tasks = await this.api.tasks(userId, this.projectId, this.filterStatus || undefined);
  }

  /* Formattage de la date */
  fmtDate(v:any): string
  {
    if (!v)
        return '';

    try
    {
      const d = new Date(v);

      if (!isNaN(d.getTime()))
        return d.toLocaleDateString();
    }
    catch {}

    if (typeof v === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(v))
        return v;

    return '';
  }

  /* Actions à faire quand on séléctionne un projet */
  onSelectProject(id:number)
  {
    this.projectId = id;
    localStorage.setItem('currentProjectId', String(id));
    this.loadRole();
    this.loadMembers();
    this.loadTasks();
  }

  /* Crée un projet */
  async createProject()
  {
    const userId = this.session.userId!;
    const p = await this.api.createProject(userId, this.pname, this.pdesc, this.pstart);
    await this.loadProjects();
    this.onSelectProject(p.id);
  }

  /* Crée une tâche pour un projet */
  async createTask()
  {
    if(!this.projectId || !this.canEdit)
        return;

    const userId = this.session.userId!;
    const payload: CreateTaskPayload = { name: this.tname, description: this.tdesc, dueDate: this.tdue, priority: this.tprio };
    await this.api.createTask(userId, this.projectId, payload);
    this.loadTasks();
  }

  /* Invite un utilisateur sur un projet */
  async invite()
  {
      if(!this.projectId || !this.inviteEmail || !this.canAdmin)
        return;
      await this.api.invite(this.projectId, this.inviteEmail);
      this.inviteEmail='';
      await this.loadMembers();
      await this.loadProjects();
  }

  /* Modifie le role d'un utilisateur pour un projet */
  async updateRole(userId:number, role:'ADMIN'|'MEMBER'|'OBSERVER')
  {
      if(!this.projectId || !this.canAdmin)
      return;

      await this.api.setRole(this.projectId, userId, role);
      await this.loadMembers();
  }

  /* Ouvre une tache */
  openTask(id:number)
  {
      this.router.navigate(['/task', id]);
  }
}
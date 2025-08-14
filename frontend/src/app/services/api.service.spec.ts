import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ApiService, CreateTaskPayload } from './api.service';

describe('ApiService', () =>
{
  let service: ApiService;
  let http: HttpTestingController;

  beforeEach(() =>
  {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService],
    });
    service = TestBed.inject(ApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('login() POSTs /api/auth/login', async () =>
  {
    const p = service.login('william@example.com', 'mdp');
    const req = http.expectOne('/api/auth/login');

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'william@example.com', password: 'mdp' });
    req.flush({ id: 1 });
    await p;
  });

  it('register() POSTs /api/auth/register', async () =>
  {
    const p = service.register('william', 'william@example.com', 'mdp');
    const req = http.expectOne('/api/auth/register');

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'william', email: 'william@example.com', password: 'mdp' });
    req.flush({ id: 2 });
    await p;
  });

  it('projects() GETs /api/projects', async () =>
  {
    const p = service.projects();
    const req = http.expectOne('/api/projects');

    expect(req.request.method).toBe('GET');
    req.flush([]);
    await p;
  });

  it('createProject() POSTs /api/projects with payload', async () =>
  {
    const p = service.createProject(1, 'N', 'D', '2025-01-01');
    const req = http.expectOne('/api/projects');

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ name: 'N', description: 'D', startDate: '2025-01-01' });
    req.flush({ id: 7 });
    await p;
  });

  it('me() GETs /api/projects/:id/me', async () =>
  {
    const p = service.me(42);
    const req = http.expectOne('/api/projects/42/me');

    expect(req.request.method).toBe('GET');
    req.flush({ role: 'ADMIN' });
    await p;
  });

  it('members() GETs /api/projects/:id/members', async () =>
  {
    const p = service.members(3);
    const req = http.expectOne('/api/projects/3/members');

    expect(req.request.method).toBe('GET');
    req.flush([]);
    await p;
  });

  it('invite() POSTs /api/projects/:id/invite with email', async () =>
  {
    const p = service.invite(9, 'x@y.z');
    const req = http.expectOne('/api/projects/9/invite');

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'x@y.z' });
    req.flush({});
    await p;
  });

  it('setRole() POSTs /api/projects/:id/roles with body', async () =>
  {
    const p = service.setRole(7, 2, 'MEMBER');
    const req = http.expectOne('/api/projects/7/roles');

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ userId: 2, role: 'MEMBER' });
    req.flush({});
    await p;
  });

  it('task() GETs /api/tasks/:id', async () =>
  {
    const p = service.task(1, 55);
    const req = http.expectOne('/api/tasks/55');

    expect(req.request.method).toBe('GET');
    req.flush({ id: 55 });
    await p;
  });

  it('tasks() without status hits /api/projects/:id/tasks (branch: false)', async () =>
  {
    const p = service.tasks(1, 12);
    const req = http.expectOne('/api/projects/12/tasks');

    expect(req.request.method).toBe('GET');
    req.flush([]);
    await p;
  });

  it('tasks() with status hits /api/projects/:id/tasks?status=XXX (branch: true)', async () =>
  {
    const p = service.tasks(1, 12, 'IN_PROGRESS');
    const req = http.expectOne('/api/projects/12/tasks?status=IN_PROGRESS');

    expect(req.request.method).toBe('GET');
    req.flush([]);
    await p;
  });

  it('createTask() POSTs /api/projects/:id/tasks with payload', async () =>
  {
    const payload: CreateTaskPayload =
    {
      name: 'T',
      description: 'D',
      dueDate: '2025-02-02',
      priority: 'HIGH',
    };

    const p = service.createTask(1, 5, payload);
    const req = http.expectOne('/api/projects/5/tasks');

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush({ id: 99 });
    await p;
  });

  it('updateTask() PUTs /api/tasks/:id with payload', async () =>
  {
    const payload = { name: 'New' };
    const p = service.updateTask(1, 77, payload);
    const req = http.expectOne('/api/tasks/77');

    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(payload);
    req.flush({ id: 77 });
    await p;
  });

  it('assignTask() POSTs /api/tasks/:id/assign with userId', async () =>
  {
    const p = service.assignTask(77, 3);
    const req = http.expectOne('/api/tasks/77/assign');

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ userId: 3 });
    req.flush({ ok: true });
    await p;
  });

  it('taskHistory() GETs /api/tasks/:id/history', async () =>
  {
    const p = service.taskHistory(77);
    const req = http.expectOne('/api/tasks/77/history');

    expect(req.request.method).toBe('GET');
    req.flush([]);
    await p;
  });

  it('addTaskHistory() POSTs /api/tasks/:id/history with body', async () =>
  {
    const body =
    {
      authorId: 1,
      action: 'UPDATED' as const,
      field: 'name' as const,
      oldValue: 'old',
      newValue: 'new',
    };

    const p = service.addTaskHistory(77, body);
    const req = http.expectOne('/api/tasks/77/history');

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});
    await p;
  });
});

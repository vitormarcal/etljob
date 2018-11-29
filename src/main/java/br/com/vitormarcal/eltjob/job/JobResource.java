package br.com.vitormarcal.eltjob.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/jobs")
public class JobResource {

    private final Scheduler scheduler;
    private final HttpServletRequest request;

    @GetMapping
    public HttpEntity<Map<String, List<String>>> listar() throws SchedulerException {

        Map<String, List<String>> collect = scheduler.getJobKeys(GroupMatcher.anyGroup())
                .stream()
                .collect(Collectors.groupingBy(
                        JobKey::getGroup,
                        Collectors.mapping(this::getLinkJob, Collectors.toList())

                ));
        return new ResponseEntity<>(collect, HttpStatus.OK);
    }

    private String getLinkJob(JobKey job) {
        String uri;
        String url = request.getRequestURL().toString();
        url = url.endsWith("/") ? url : url + "/";
        if (job.getName().equals(job.getGroup())) {
            uri = job.getName();
        } else {
            uri = job.getGroup() + "/" + job.getName();
        }

        return url + uri;
    }


    @RequestMapping({"/{grupo}/{nome}", "/{nome}"})
    public ResponseEntity<String> excutar(@PathVariable(required = false) String grupo, @PathVariable String nome) throws SchedulerException {
        JobKey key = JobKey.jobKey(nome, Optional.ofNullable(grupo).orElse(nome));
        if (scheduler.checkExists(key)) {
            if (jobEmExecucao(key)) {
                return new ResponseEntity<>("Job " + nome + " já está em execução", HttpStatus.LOCKED);
            }
            scheduler.triggerJob(key);
            return ResponseEntity.ok("Job " + nome + " inicializado com sucesso");
        }
        return ResponseEntity.notFound().build();
    }

    private boolean jobEmExecucao(JobKey key) throws SchedulerException {
        return scheduler.getCurrentlyExecutingJobs()
                .stream()
                .anyMatch(e -> e.getJobDetail().getKey().getGroup().equalsIgnoreCase(key.getGroup()) &&
                        e.getJobDetail().getKey().getName().equalsIgnoreCase(key.getName()));

    }

}

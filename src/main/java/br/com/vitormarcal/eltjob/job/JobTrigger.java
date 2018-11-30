package br.com.vitormarcal.eltjob.job;


import br.com.vitormarcal.eltjob.config.AutowiringSpringBeanJobFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.List;
import java.util.stream.Collectors;

import static org.quartz.JobBuilder.newJob;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class JobTrigger {

    private final List<JobService> jobs;
    private final ApplicationContext applicationContext;

    @Bean
    public SchedulerFactoryBean quartzScheduler() {
        SchedulerFactoryBean quartzScheduler = new SchedulerFactoryBean();
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        quartzScheduler.setJobFactory(jobFactory);
        return quartzScheduler;
    }

    @Bean
    public Scheduler scheduler() throws SchedulerException {
        List<Detail> details = jobs.stream()
                .filter(JobService::isActive)
                .map(this::createJob)
                .collect(Collectors.toList());

        Scheduler scheduler = quartzScheduler().getScheduler();
        details.forEach(d -> {
            try {
                scheduler.scheduleJob(d.jobDetail, d.cronTrigger);
                log.info("Registrado job [{}] do grupo [{}]", d.jobDetail.getKey().getName(), d.jobDetail.getKey().getGroup());
            } catch (SchedulerException e) {
                throw new RuntimeException("Erro ao registrar agendamento do job", e);
            }
        });

        scheduler.start();
        return scheduler;
    }

    private Detail createJob(JobService jobService) {
        JobDetail jobDetail = newJob(jobService.getClass()).withIdentity(jobService.getJobName(), jobService.getGroupName()).build();
        CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity(jobService.getJobName(), jobService.getGroupName())
                .withSchedule(CronScheduleBuilder.cronSchedule(jobService.getCronExpression()))
                .build();
        return new Detail(jobDetail, cronTrigger);
    }

    @RequiredArgsConstructor
    private class Detail {
        private final JobDetail jobDetail;
        private final CronTrigger cronTrigger;
    }

}

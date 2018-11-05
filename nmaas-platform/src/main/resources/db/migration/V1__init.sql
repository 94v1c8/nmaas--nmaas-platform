create table ansible_playbook_vpn_config (
  id bigint generated by default as identity,
  asn varchar(255),
  bgp_group_id varchar(255),
  bgp_local_cidr varchar(255),
  bgp_local_ip varchar(255),
  bgp_neighbor_ip varchar(255),
  interface_unit varchar(255),
  interface_vlan varchar(255),
  logical_interface varchar(255),
  physical_interface varchar(255),
  policy_community_options varchar(255),
  policy_statement_connected varchar(255),
  policy_statement_export varchar(255),
  policy_statement_import varchar(255),
  target_router varchar(255),
  target_router_id varchar(255),
  type integer,
  vrf_id varchar(255),
  vrf_rd varchar(255),
  vrf_rt varchar(255),
  primary key (id));

create table app_deployment (
  id bigint generated by default as identity,
  application_id bytea not null,
  config_file_repository_required boolean not null,
  deployment_id bytea not null,
  deployment_name varchar(255) not null,
  domain varchar(255) not null,
  error_message text,
  state varchar(255) not null,
  storage_space integer,
  configuration_id bigint,
  primary key (id));

create table app_deployment_configuration (
  id bigint generated by default as identity,
  json_input varchar(10485760) not null, primary key (id));

create table app_deployment_history (
  id bigint generated by default as identity,
  current_state integer not null,
  previous_state integer,
  timestamp timestamp not null,
  app_id bigint not null,
  primary key (id));

create table app_deployment_spec (
  id bigint generated by default as identity,
  config_file_repository_required boolean not null,
  default_storage_space integer not null,
  docker_compose_file_template_id bigint,
  kubernetes_template_id bigint,
  primary key (id));

create table app_deployment_spec_deploy_parameters (
  app_deployment_spec_id bigint not null,
  deploy_parameters varchar(255),
  deploy_parameters_key integer not null,
  primary key (app_deployment_spec_id, deploy_parameters_key));

create table app_deployment_spec_supported_deployment_environments (
  app_deployment_spec_id bigint not null,
  supported_deployment_environments varchar(255));

create table app_instance (
  id bigint generated by default as identity,
  configuration varchar(10485760),
  created_at bigint not null,
  internal_id bytea,
  name varchar(255),
  domain_id bigint not null,
  application_application_id bigint not null,
  owner_id bigint,
  primary key (id));

create table application (
  application_id bigint generated by default as identity,
  brief_description varchar(255),
  deleted boolean not null,
  full_description text,
  issues_url varchar(255),
  license varchar(255),
  name varchar(255),
  source_url varchar(255),
  version varchar(255),
  www_url varchar(255),
  additional_parameters_template_id bigint,
  app_deployment_spec_id bigint,
  config_template_id bigint,
  logo_id bigint,
  primary key (application_id),
  constraint u_application unique (name,version));

create table application_screenshots (
  application_application_id bigint not null,
  screenshots_id bigint not null unique);

create table application_tag (
  application_id bigint not null,
  tag_id bigint not null,
  primary key (application_id, tag_id));

create table application_subscription (
  active boolean not null,
  deleted boolean not null,
  domain_id bigint not null,
  application_application_id bigint not null,
  primary key (application_application_id, domain_id));

create table app_rate (
  application_id bigint not null,
  user_id bigint not null,
  rate integer,
  primary key (application_id, user_id));

create table comment (
  id bigint generated by default as identity,
  comment varchar(255),
  created_at bigint not null,
  deleted boolean not null,
  application_application_id bigint not null,
  owner_id bigint,
  parent_id bigint,
  primary key (id));

create table config_template (
  id bigint generated by default as identity,
  template text not null,
  primary key (id));

create table configuration (
  id bigint generated by default as identity,
  maintenance boolean not null,
  sso_login_allowed boolean not null,
  primary key (id));

create table content (
  id bigint generated by default as identity,
  content varchar(10485760),
  name varchar(255) not null,
  title varchar(255),
  primary key (id));

create table dcn_attached_container (
  id bigint generated by default as identity,
  description varchar(255) not null,
  name varchar(255) not null,
  primary key (id));

create table dcn_cloud_endpoint_details (
  id bigint generated by default as identity,
  gateway varchar(255) not null,
  subnet varchar(255) not null,
  vlan_number integer not null,
  primary key (id));

create table dcn_info (
  id bigint generated by default as identity,
  domain varchar(255) not null,
  name varchar(255) not null,
  state varchar(255) not null,
  cloud_endpoint_details_id bigint,
  playbook_for_client_side_router_id bigint,
  playbook_for_cloud_side_router_id bigint,
  primary key (id));

create table docker_compose_file (
  id bigint generated by default as identity,
  compose_file_content text not null,
  primary key (id));

create table docker_compose_file_template (
  id bigint generated by default as identity,
  compose_file_template_content text not null,
  primary key (id));

create table docker_compose_file_template_dcn_attached_containers (
  docker_compose_file_template_id bigint not null,
  dcn_attached_containers_id bigint not null unique);

create table docker_compose_service (
  id bigint generated by default as identity,
  attached_volume_name varchar(255) not null,
  dcn_network_name varchar(255),
  external_access_network_name varchar(255) not null,
  public_port integer not null,
  primary key (id));

create table docker_compose_service_component (
  id bigint generated by default as identity,
  deployment_name varchar(255) not null,
  description varchar(255) not null,
  ip_address_of_container varchar(255) not null,
  name varchar(255) not null,
  primary key (id));

create table docker_compose_service_service_components (
  docker_compose_service_id bigint not null,
  service_components_id bigint not null unique);

create table docker_host (
  id bigint generated by default as identity,
  access_interface_name varchar(255),
  api_ip_address bytea,
  api_port integer,
  base_data_network_address bytea,
  data_interface_name varchar(255),
  name varchar(255),
  preferred boolean not null,
  public_ip_address bytea,
  volumes_path varchar(255),
  working_path varchar(255),
  primary key (id));

create table docker_host_attach_point (
  id bigint generated by default as identity,
  docker_host_name varchar(255) not null unique,
  router_id varchar(255) not null,
  router_interface_name varchar(255) not null,
  router_name varchar(255) not null,
  primary key (id));

create table docker_host_network (
  id bigint generated by default as identity,
  deployment_id varchar(255),
  deployment_name varchar(255),
  domain varchar(255) not null,
  gateway varchar(255),
  subnet varchar(255),
  vlan_number integer not null,
  host_id bigint not null,
  primary key (id));

create table docker_host_state (
  id bigint generated by default as identity,
  docker_host_address_pool_base varchar(255) not null,
  docker_host_name varchar(255) not null,
  primary key (id));

create table docker_host_state_address_assignments (
  docker_host_state_id bigint not null,
  address_assignments_id bigint not null unique);

create table docker_host_state_port_assignments (
  docker_host_state_id bigint not null,
  port_assignments_id bigint not null unique);

create table docker_host_state_vlan_assignments (
  docker_host_state_id bigint not null,
  vlan_assignments_id bigint not null unique);

create table docker_network_ipam_spec (
  id bigint generated by default as identity,
  gateway varchar(255) not null,
  ip_address_of_container varchar(255) not null,
  ip_range_with_mask varchar(255) not null,
  subnet_with_mask varchar(255) not null,
  primary key (id));

create table docker_compose_nm_service_info (
  id bigint not null,
  docker_compose_file_id bigint,
  docker_compose_file_template_id bigint,
  docker_compose_service_id bigint,
  host_id bigint,
  primary key (id));

create table docker_host_network_assigned_addresses (
  docker_host_network_id bigint not null,
  assigned_addresses varchar(255));

create table domain (
  id bigint generated by default as identity,
  active boolean not null,
  codename varchar(255) not null unique,
  dcn_configured boolean not null,
  kubernetes_namespace varchar(255),
  kubernetes_storage_class varchar(255),
  name varchar(255) not null unique,
  primary key (id));

create table domain_network_attach_point (
  id bigint generated by default as identity,
  as_number varchar(255) not null,
  bgp_local_ip varchar(255) not null,
  bgp_neighbor_ip varchar(255) not null,
  domain varchar(255) not null,
  router_id varchar(255) not null,
  router_interface_name varchar(255) not null,
  router_interface_unit varchar(255) not null,
  router_interface_vlan varchar(255) not null,
  router_name varchar(255) not null,
  monitored_equipment_id bigint,
  primary key (id));

create table domain_network_monitored_equipment (
  id bigint generated by default as identity,
  primary key (id));

create table domain_network_monitored_equipment_addresses (
  domain_network_monitored_equipment_id bigint not null,
  addresses varchar(255));

create table domain_network_monitored_equipment_networks (
  domain_network_monitored_equipment_id bigint not null,
  networks varchar(255));

create table file_info (
  id bigint generated by default as identity,
  content_type varchar(255),
  filename varchar(255),
  primary key (id));

create table gitlab (
  id bigint generated by default as identity,
  port integer not null,
  repository_access_username varchar(255) not null,
  server varchar(255) not null,
  ssh_server varchar(255) not null,
  token varchar(255) not null,
  primary key (id));

create table gitlab_project (
  id bigint generated by default as identity,
  access_password varchar(255) not null,
  access_url varchar(255) not null,
  access_user varchar(255) not null,
  clone_url varchar(255) not null,
  deployment_id bytea not null,
  primary key (id));

create table k_cluster (
  id bigint generated by default as identity,
  api_id bigint not null unique,
  attach_point_id bigint,
  deployment_id bigint not null unique,
  helm_id bigint not null unique,
  ingress_id bigint not null unique,
  primary key (id));

create table k_cluster_api (
  id bigint generated by default as identity,
  rest_api_host_address bytea,
  rest_api_port integer,
  usekcluster_api boolean not null,
  primary key (id));

create table k_cluster_attach_point (
  id bigint generated by default as identity,
  router_id varchar(255) not null,
  router_interface_name varchar(255) not null,
  router_name varchar(255) not null,
  primary key (id));

create table k_cluster_deployment (
  id bigint generated by default as identity,
  default_namespace varchar(255),
  default_storage_class varchar(255),
  namespace_config_option integer not null,
  smtp_server_hostname varchar(255) not null,
  smtp_server_password varchar(255),
  smtp_server_port integer not null,
  smtp_server_username varchar(255),
  use_in_cluster_git_lab_instance boolean not null,
  primary key (id));

create table k_cluster_ext_network (
  id bigint generated by default as identity,
  assigned boolean not null,
  assigned_since timestamp,
  assigned_to varchar(255),
  external_ip bytea not null,
  external_network bytea not null,
  external_network_mask_length integer not null,
  primary key (id));

create table k_cluster_external_networks (
  kcluster_id bigint not null,
  external_networks_id bigint not null unique);

create table k_cluster_helm (
  id bigint generated by default as identity,
  enable_tls boolean,
  helm_chart_repository_name varchar(255),
  helm_host_address bytea not null,
  helm_host_charts_directory varchar(255),
  helm_host_ssh_username varchar(255) not null,
  use_local_chart_archives boolean not null,
  primary key (id));

create table k_cluster_ingress
  (id bigint generated by default as identity,
  controller_chart_archive varchar(255),
  controller_chart_name varchar(255),
  controller_config_option varchar(255) not null,
  external_service_domain varchar(255),
  resource_config_option varchar(255) not null,
  supported_ingress_class varchar(255),
  tls_supported boolean,
  primary key (id));

create table kubernetes_chart (
  id bigint generated by default as identity,
  name varchar(255) not null,
  version varchar(255),
  primary key (id));

create table kubernetes_nm_service_info (
  service_external_url varchar(255),
  id bigint not null,
  kubernetes_template_id bigint,
  primary key (id));

create table kubernetes_template (
  id bigint generated by default as identity,
  archive varchar(255),
  chart_id bigint,
  primary key (id));

create table monitor_entry (
  id bigint generated by default as identity,
  check_interval bigint not null,
  last_check timestamp,
  last_success timestamp,
  service_name integer not null,
  status integer,
  time_format integer,
  primary key (id));

create table nm_service_configuration (
  id bigint generated by default as identity,
  config_file_content text not null,
  config_file_name varchar(255) not null,
  config_id varchar(255) not null,
  primary key (id));

create table nm_service_configuration_template (
  id bigint generated by default as identity,
  application_id bigint not null,
  config_file_name varchar(255) not null,
  config_file_template_content text not null,
  primary key (id));

create table nm_service_info (
  id bigint generated by default as identity,
  deployment_id bytea not null,
  deployment_name varchar(255) not null,
  domain varchar(255) not null,
  name varchar(255) not null,
  state integer not null,
  storage_space integer not null,
  git_lab_project_id bigint,
  primary key (id));

create table nm_service_info_additional_parameters (
  nm_service_info_id bigint not null,
  additional_parameters varchar(255),
  additional_parameters_key varchar(255) not null,
  primary key (nm_service_info_id, additional_parameters_key));

create table nm_service_info_managed_devices_ip_addresses (
  nm_service_info_id bigint not null,
  managed_devices_ip_addresses varchar(255));

create table number_assignment (
  id bigint generated by default as identity,
  number integer not null,
  owner_id bytea not null,
  primary key (id));

create table tag (
  tag_id bigint generated by default as identity,
  name varchar(255),
  primary key (tag_id));

create table user_role (
  role varchar(255) not null,
  user_id bigint not null,
  domain_id bigint not null,
  primary key (domain_id, role, user_id));

create table users (
  id bigint generated by default as identity,
  email varchar(255),
  enabled boolean not null,
  firstname varchar(255),
  lastname varchar(255),
  password varchar(255),
  privacy_policy_accepted boolean not null,
  saml_token varchar(255),
  terms_of_use_accepted boolean not null,
  username varchar(255) not null,
  primary key (id));

create table internationalization (
  id bigint generated by default as identity,
  language varchar(255),
  content varchar(10485760),
  primary key (id));

--TODO: Try to replace with DDL while avoiding sequence conflict
alter table app_deployment add constraint FKflw7k2c6wsh8whiqveuktib6y foreign key (configuration_id) references app_deployment_configuration;
alter table app_deployment_history add constraint FKn03wx0nqdokhdjau5e5l6nniv foreign key (app_id) references app_deployment;
alter table app_deployment_spec add constraint FKkl5ydkj3n7tg4cio1kb6rux32 foreign key (docker_compose_file_template_id) references docker_compose_file_template;
alter table app_deployment_spec add constraint FKm66qvt95dxqj0i2w3ss1wk793 foreign key (kubernetes_template_id) references kubernetes_template;
alter table app_deployment_spec_deploy_parameters add constraint FKosr9xmrexbp8i9o8nsl6oufl foreign key (app_deployment_spec_id) references app_deployment_spec;
alter table app_deployment_spec_supported_deployment_environments add constraint FKn0dxswsj3x91wpubo1wbgmf9k foreign key (app_deployment_spec_id) references app_deployment_spec;
alter table app_instance add constraint FKf0p9u1tfnbv93ijqlud3xyvps foreign key (domain_id) references domain;
alter table app_instance add constraint FKetmli4xeu401cio7pfe138oif foreign key (application_application_id) references application;
alter table app_instance add constraint FK5gip41biojnv5nlbfekulm9aa foreign key (owner_id) references users;
alter table application add constraint FK9761cddsjbnr4lcmjgmm8ylc4 foreign key (additional_parameters_template_id) references config_template;
alter table application add constraint FKkxj4cvi3xu9vuie30sed249gj foreign key (app_deployment_spec_id) references app_deployment_spec;
alter table application add constraint FKcihisdtrotpgh6li39odeg661 foreign key (config_template_id) references config_template;
alter table application add constraint FKpx2rhjvxsdbsutptf4s2u2ts3 foreign key (logo_id) references file_info;
alter table application_screenshots add constraint FKiwgo811ak9ahp02x0ikh0tr3n foreign key (screenshots_id) references file_info;
alter table application_screenshots add constraint FKdslj6b1a82lby4f4bvln27883 foreign key (application_application_id) references application;
alter table application_tag add constraint FKhuqj565iwm2wph71q2sx76yuv foreign key (tag_id) references tag;
alter table application_tag add constraint FK99x3mt75oqu7elqyo6hjfxp2e foreign key (application_id) references application;
alter table application_subscription add constraint FKob0dttod0dnjkbx0voqkqojrn foreign key (domain_id) references domain;
alter table application_subscription add constraint FKnp15lbug6jtxwyl46qtkambd4 foreign key (application_application_id) references application;
alter table comment add constraint FKl1ijg86at535f02qrnag2884f foreign key (application_application_id) references application;
alter table comment add constraint FKcjptd4mjai64kvah9b6cbquer foreign key (owner_id) references users;
alter table comment add constraint FKde3rfu96lep00br5ov0mdieyt foreign key (parent_id) references comment;
alter table dcn_info add constraint FK6tnvn4d10w4gamkiwyettj0xn foreign key (cloud_endpoint_details_id) references dcn_cloud_endpoint_details;
alter table dcn_info add constraint FKa60cx3nls8rc7omsglkb2gbxh foreign key (playbook_for_client_side_router_id) references ansible_playbook_vpn_config;
alter table dcn_info add constraint FKm88vjmyolbu15ccg5s58xp3a9 foreign key (playbook_for_cloud_side_router_id) references ansible_playbook_vpn_config;
alter table docker_compose_file_template_dcn_attached_containers add constraint FK5xd604ib0j1qck9ygeevwphrl foreign key (dcn_attached_containers_id) references dcn_attached_container;
alter table docker_compose_file_template_dcn_attached_containers add constraint FKdly170qtj2rijr4rve7xomq2b foreign key (docker_compose_file_template_id) references docker_compose_file_template;
alter table docker_compose_service_service_components add constraint FKn9hsaiuh5404bd6ej62n1ah0y foreign key (service_components_id) references docker_compose_service_component;
alter table docker_compose_service_service_components add constraint FKbvyqskquvj9rodwtotvcn94xb foreign key (docker_compose_service_id) references docker_compose_service;
alter table docker_host_network add constraint FK423fvthlt7g42g009axbvbo9x foreign key (host_id) references docker_host;
alter table docker_host_state_address_assignments add constraint FKjhmcdqf7pb1nx54w82og2bcxq foreign key (address_assignments_id) references number_assignment;
alter table docker_host_state_address_assignments add constraint FKrn2uba1i7fcrpcxs0gffona6q foreign key (docker_host_state_id) references docker_host_state;
alter table docker_host_state_port_assignments add constraint FKcw5klnilsnphhbkakpeffrnpu foreign key (port_assignments_id) references number_assignment;
alter table docker_host_state_port_assignments add constraint FKt8el0ef0yvh1bxbogn8ygcrxe foreign key (docker_host_state_id) references docker_host_state;
alter table docker_host_state_vlan_assignments add constraint FK4brlydugv5qu6irxsd8b6r3c2 foreign key (vlan_assignments_id) references number_assignment;
alter table docker_host_state_vlan_assignments add constraint FKcaakbe3ked411x9kd19c3x91q foreign key (docker_host_state_id) references docker_host_state;
alter table docker_compose_nm_service_info add constraint FKcpij2menj0vglapskrygwjb02 foreign key (docker_compose_file_id) references docker_compose_file;
alter table docker_compose_nm_service_info add constraint FKnl0val315yk6bg82sdl04wkcc foreign key (docker_compose_file_template_id) references docker_compose_file_template;
alter table docker_compose_nm_service_info add constraint FK93unwii01ncm30rtvfq2wmkd8 foreign key (docker_compose_service_id) references docker_compose_service;
alter table docker_compose_nm_service_info add constraint FKai8jxs73fq5cqtuskgd1rv60j foreign key (host_id) references docker_host;
alter table docker_compose_nm_service_info add constraint FKny881tbowo3qyfo5is3r1byp6 foreign key (id) references nm_service_info;
alter table docker_host_network_assigned_addresses add constraint FKhx905suq0j26a1p4jym18xipu foreign key (docker_host_network_id) references docker_host_network;
alter table domain_network_attach_point add constraint FKnd4yl29c4sr9ldud32xkmvktb foreign key (monitored_equipment_id) references domain_network_monitored_equipment;
alter table domain_network_monitored_equipment_addresses add constraint FKgsak8ane4pv0tlwihi15vv2wu foreign key (domain_network_monitored_equipment_id) references domain_network_monitored_equipment;
alter table domain_network_monitored_equipment_networks add constraint FKbgehd581e79bklurbx6lbgrb5 foreign key (domain_network_monitored_equipment_id) references domain_network_monitored_equipment;
alter table k_cluster add constraint FKn1qe22ah6w6fkja6on98myqw7 foreign key (api_id) references k_cluster_api;
alter table k_cluster add constraint FKnyns7j7jth9mnpqp5skelsoxe foreign key (attach_point_id) references k_cluster_attach_point;
alter table k_cluster add constraint FKk16erung622e0exwmpw9d8bup foreign key (deployment_id) references k_cluster_deployment;
alter table k_cluster add constraint FK6035c9caxx6e0k5l2aoo59ygq foreign key (helm_id) references k_cluster_helm;
alter table k_cluster add constraint FKrpqeqymv7549571ief9prgyv1 foreign key (ingress_id) references k_cluster_ingress;
alter table k_cluster_external_networks add constraint FKd79mobwmaocm1d7bk95v1a24h foreign key (external_networks_id) references k_cluster_ext_network;
alter table k_cluster_external_networks add constraint FKetvwdm7jgq1jr8l47u66w6up5 foreign key (kcluster_id) references k_cluster;
alter table kubernetes_nm_service_info add constraint FK7w6u2g82dloa3yew9lu8i0fr2 foreign key (kubernetes_template_id) references kubernetes_template;
alter table kubernetes_nm_service_info add constraint FKk0fuxaguvful55skhuraxrd6j foreign key (id) references nm_service_info;
alter table kubernetes_template add constraint FKjxm6n0mpu7u0jdr51qjlwii2h foreign key (chart_id) references kubernetes_chart;
alter table nm_service_info add constraint FK2ffsbhimeos4lmxhcbfwcl6lr foreign key (git_lab_project_id) references gitlab_project;
alter table nm_service_info_additional_parameters add constraint FKt80oxva42ec2fqg0gypq2moc3 foreign key (nm_service_info_id) references nm_service_info;
alter table nm_service_info_managed_devices_ip_addresses add constraint FKhfwvis7mkxbwnabi9oya4bvcw foreign key (nm_service_info_id) references nm_service_info;
alter table user_role add constraint FKj345gk1bovqvfame88rcx7yyx foreign key (user_id) references users;
alter table user_role add constraint FK6skmr1hqmklgohtssd74qkrau foreign key (domain_id) references domain;
alter table app_deployment add (logged_in_users_name varchar(255), domain_id  bigint, app_name  varchar(255), app_instance_id bigint, app_instance_name varchar(255), domain_code_name varchar(255));
